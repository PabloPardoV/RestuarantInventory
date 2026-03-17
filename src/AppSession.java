// AppSession.java
// Stores data that needs to be shared between screens without
// passing variables around manually. Any class can read or write
// the current restaurant ID through the static methods here.

public class AppSession {

    // Stores which restaurant is currently selected.
    // 1 = Restaurant A, 2 = Restaurant B.
    // -1 means the user has not selected a restaurant yet.
    private static int currentRestaurantId = -1;

    // Saves the restaurant ID when the user picks Restaurant A or B on the login screen
    public static void setCurrentRestaurantId(int restaurantId) {
        currentRestaurantId = restaurantId;
    }

    // Returns the ID of the restaurant the user is currently working with.
    // All DAO methods call this to know which restaurant's data to query.
    public static int getCurrentRestaurantId() {
        return currentRestaurantId;
    }
}
