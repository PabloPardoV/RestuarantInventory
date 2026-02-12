//Stores session-wide values that multiple screens need to access.This avoids passing variables through every GUI manually.
public class AppSession
{

    // Stores which restaurant is currently selected (1 = A, 2 = B)
    private static int currentRestaurantId = -1;

    /**
     * Sets the restaurant ID when the user selects Restaurant A or B.
     */
    public static void setCurrentRestaurantId(int restaurantId) {
        currentRestaurantId = restaurantId;
    }

    /**
     * Returns the currently selected restaurant ID.
     */
    public static int getCurrentRestaurantId() {
        return currentRestaurantId;
    }
}