package ir.maktab.services.impl;

import ir.maktab.Dao.PostRepository;
import ir.maktab.Dao.impl.PostRepositoryImpl;
import ir.maktab.MyApp;
import ir.maktab.Scan;
import ir.maktab.base.services.impl.BaseServiceImpl;
import ir.maktab.domains.Comment;
import ir.maktab.domains.Post;
import ir.maktab.domains.User;
import ir.maktab.services.PostService;
import ir.maktab.services.UserService;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;


public class PostServiceImpl extends BaseServiceImpl<Post,Long, PostRepository> implements PostService {
    private Scan sc;
    private Consumer<Post> addLikeOrComment;
    private UserService userService;
    public PostServiceImpl() {
        PostRepository postRepository = new PostRepositoryImpl();
        sc = MyApp.getSc();
        userService = MyApp.getUserService();
        //Consumer!
        addLikeOrComment = (c) -> {
            if (c.getImage() == null){
                System.out.println(c);
            }else {
                byte[] img = c.getImage();
                try {
                    FileOutputStream fos = new FileOutputStream("output.jpg");
                    fos.write(img);
                    fos.close();
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(new File("output.jpg"));
                    System.out.println(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String choice = sc.getString("Comment Or Like Or Both Or Pass: ").toUpperCase();
            User user = UserServiceImpl.getUser();
            switch (choice) {
                case "LIKE":
                    c.addLike(user);
                    user.addPostLiked(c);
                    break;
                case "COMMENT":
                    addCommentToPost(c,user);
                    break;
                case "BOTH":
                    c.addLike(user);
                    addCommentToPost(c,user);
                    break;
                default:
            }
            if (!choice.equals("PASS")) {
                baseRepository.saveOrUpdate(c);
            }
            File file = new File("output.jpg");
            file.delete();
        };
        super.setRepository(postRepository);
    }

    @Override
    public void insert() {
        String content = sc.getString("Contents Of Post: ");
        Post post = new Post();
        char choice = sc.getString("Add Image: Y/N : ").toUpperCase().charAt(0);
        if(choice == 'Y'){
            String path = sc.getString("Path: ");
            File file = new File(path);
            byte[] bFile = new byte[(int) file.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(bFile);
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            post.setImage(bFile);
        }
        post.setContent(content);
        post.setUser(UserServiceImpl.getUser());
        post.setDate(new Date());
        baseRepository.saveOrUpdate(post);
    }

    @Override
    public void displayLikedPosts() {
        User u = UserServiceImpl.getUser();
        Set<Post> postsLiked = u.getPostsLiked();
        if(postsLiked != null){
            int i = 0;
            Iterator<Post> it = postsLiked.iterator();
            while(it.hasNext() && i < 5){
                System.out.println(it.next());
                i++;
            }
        }else{
            System.out.println("No Post Liked!");
        }
    }

    @Override
    public void displayFollowingsPosts() {
        userService.displayFollowings();
        String userName = sc.getString("Username: ");
        User user = userService.findByUserName(userName);
        user.getPosts()
                .forEach(addLikeOrComment);
    }

    @Override
    public void displayDailyPosts() {
        User user = UserServiceImpl.getUser();
        Set<User> followings = user.getFollowings();
        followings.stream()
                .map(User::getPosts)
                .forEach(posts -> posts.stream()
                        .filter((c) -> c.getDate().compareTo(user.getDate()) > 0)
                        .forEach(addLikeOrComment));
    }

    private void addCommentToPost(Post c,User user) {
        Comment comment = new Comment();
        String content = sc.getString("Comment: ");
        comment.setComment(content);
        comment.setUser(user);
        comment.setPost(c);
        System.out.println(comment);
    }
//    @Override
//    public void displayCommentedPosts() {
//        User u = UserServiceImpl.getUser();
//        Set<Comment> comments = u.getComments();
//        if(comments != null){
//            int i = 0;
//            Iterator<Comment> it = comments.iterator();
//            while(it.hasNext() && i < 5){
//                System.out.println(it.next().getPost());
//                i++;
//            }
//        }else{
//            System.out.println("No Post Commented");
//        }
//    }

    @Override
    public void displayUsersPosts() {
        User u = UserServiceImpl.getUser();
        displayPosts(u);
    }

    private void displayPosts(User u) {
        Set<Post> posts = u.getPosts();
        posts.forEach(addLikeOrComment);
    }

}
