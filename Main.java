import modelos.DadosCartao;
import modelos.Pedido;
import modelos.ResultadoPedido;

public class Main {
    public static void main(String[] args) {
        Pedido pedido = new Pedido(
            "PROD-42",
            2,
            1.5,
            new DadosCartao("1234567890123456", "123", "12/27"),
            249.90,
            "01310-100",
            "cliente@email.com",
            "(11) 99999-0000"
        );

        PedidoFacade facade = new PedidoFacade();
        ResultadoPedido resultado = facade.finalizarPedido(pedido);
        System.out.println(resultado);
        
        //testando consulta de status
        System.out.println("\nConsultando status do pedido...");
        String status = facade.consultarStatus(resultado.transacaoId);
        System.out.println(status);

        //testando cancelamento do pedido
        System.out.println("\nCancelando pedido...");
        resultado = facade.cancelarPedido(pedido.produtoId, pedido.quantidade, resultado.transacaoId);
        System.out.println(resultado);

        //consultando status após cancelamento
        System.out.println("\nConsultando status após cancelamento...");
        status = facade.consultarStatus(resultado.transacaoId);
        System.out.println(status);
    }
}