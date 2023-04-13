package persistent;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import exception.InvalidSwipeDataException;
import models.SwipeData;
import models.SwipeDetails;
import org.bson.Document;

import java.util.List;

public class SwipeLikesRepository {

    private static final String LIKE = "right";
    private static final String DATABASE = "swipe_data";
    private static final String COLLECTION = "swipe_likes";
    private static final String SWIPEE = "swipee";
    private static final String LIKES = "likes";
    private static final String DISLIKES = "dislikes";

    private static final String SERVER_URI = "localhost:27017";

    public static void updateSwipeLikes(SwipeData swipeData) throws InvalidSwipeDataException {
        SwipeDetails swipeDetails = swipeData.getSwipeDetails();
        if (swipeData == null) {
            throw new InvalidSwipeDataException();
        }
        String swipee = swipeDetails.getSwipee();
        String swiper = swipeDetails.getSwiper();
        MongoClient mongoClient = MongoClients.create(SERVER_URI);
        MongoDatabase db = mongoClient.getDatabase(DATABASE);
        MongoCollection<Document> collection = db.getCollection(COLLECTION);
        if (collection == null) {
            db.createCollection(COLLECTION);
        }
        Document searchQuery = new Document();
        searchQuery.put(SWIPEE, swipee);
        FindIterable<Document> cursor = collection.find(searchQuery);
        Document doc = cursor.first();
        Long likes = (Long) doc.get(LIKES);
        Long dislikes = (Long) doc.get(DISLIKES);
        boolean isLike = LIKE.equals(swipeData.getLeftOrRight()) ? true : false;
        if (isLike) {
            likes = likes + 1;
        } else {
            dislikes = dislikes + 1;
        }
        Document newDoc = new Document()
                .append(SWIPEE, swipee)
                .append(LIKES, likes)
                .append(DISLIKES, dislikes);
        collection.updateMany(Filters.eq(SWIPEE, swipee), newDoc);
    }
}
