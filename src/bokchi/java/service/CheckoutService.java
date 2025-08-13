package bokchi.java.service;

import bokchi.java.model.UserVO;
import bokchi.java.ui.user.CartPanel;
import java.sql.SQLException;
import java.util.List;

public interface CheckoutService {
    OrderResult checkout(UserVO customer,
                         List<CartPanel.CartLine> lines,
                         int uiTotalAmount,
                         boolean usedFreeDrink,
                         Integer freeDrinkItemId) throws SQLException;

    record OrderResult(int orderId, int totalAmount, int stampsEarned, boolean usedFreeDrink) {}
}