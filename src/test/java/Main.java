import com.mySpring.myServlet.MyDispatchServlet;

import javax.servlet.ServletException;

public class Main {
    public static void main(String[] args) throws ServletException {
        MyDispatchServlet myDispatchServlet = new MyDispatchServlet();
        myDispatchServlet.init();
    }
}
