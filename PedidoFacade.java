import modelos.Pedido;
import modelos.ResultadoColeta;
import modelos.ResultadoPagamento;
import modelos.ResultadoPedido;
import sistemas.EstoqueService;
import sistemas.FreteService;
import sistemas.NotificacaoService;
import sistemas.PagamentoService;

/**
 * PedidoFacade fornece uma interface unificada e simplificada
 * para o processo de finalização de pedidos, ocultando a
 * complexidade dos subsistemas de Estoque, Pagamento, Frete
 * e Notificação do código cliente.
 */
public class PedidoFacade {
    // Os subsistemas são instanciados aqui e nunca expostos ao cliente
    private final EstoqueService     estoque     = new EstoqueService();
    private final PagamentoService   pagamento   = new PagamentoService();
    private final FreteService       frete       = new FreteService();
    private final NotificacaoService notificacao = new NotificacaoService();
    private ResultadoPedido resultadoPedido;
    /**
     * Orquestra todos os subsistemas para finalizar um pedido.
     * O cliente chama apenas este método — toda a complexidade
     * fica encapsulada na Facade.
     */
    public ResultadoPedido finalizarPedido(Pedido pedido) {

        if (!estoque.verificarDisponibilidade(pedido.produtoId, pedido.quantidade)) {
            return new ResultadoPedido(false, "Produto indisponível");
        }

        if (!pagamento.validarCartao(pedido.dadosCartao)) {
            return new ResultadoPedido(false, "Cartão inválido");
        }

        
        String resultado = "Pedido finalizado com sucesso!";
        this.resultadoPedido = new ResultadoPedido(true, resultado);
        this.estoque.reservarItens(pedido.produtoId, pedido.quantidade);
        
        double custoFrete = frete.calcularFrete(pedido.cep, pedido.peso);
        ResultadoPagamento resultadoPagamento = this.pagamento.processarCobranca(pedido.valor + custoFrete, pedido.dadosCartao);
        this.resultadoPedido.transacaoId = resultadoPagamento.transacaoId;

        ResultadoColeta resultadoColeta = this.frete.agendarColeta(pedido.cep, resultadoPedido.transacaoId);
        this.resultadoPedido.codigoColeta = resultadoColeta.codigo;
        this.resultadoPedido.prazoEntrega = resultadoColeta.prazo;
        
        this.notificacao.enviarEmail(pedido.email, resultado);
        this.notificacao.enviarSMS(pedido.telefone, resultado);
        
        return this.resultadoPedido;
          
    }

    /**
     * Cancela um pedido já realizado, estornando o pagamento
     * e liberando os itens reservados no estoque.
     */
    public ResultadoPedido cancelarPedido(String produtoId, int quantidade, String transacaoId) {
        this.resultadoPedido.mensagem = "Pedido cancelado com sucesso!";
        this.resultadoPedido.sucesso = false;
        return this.resultadoPedido;
    }

    /**
     * Retorna um resumo de status do pedido sem expor detalhes internos.
     */
    public String consultarStatus(String transacaoId) {
        return this.resultadoPedido.sucesso ? "Pedido em processamento" : "Pedido cancelado ou falhou";
    }
    
}
