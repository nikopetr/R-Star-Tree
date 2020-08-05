public class Main {
    public static void main(String[] args){
        boolean reconstructingTree = UserInterface.startApplication();
        RStarTree rStarTree = new RStarTree(reconstructingTree);
        UserInterface.runApplication(rStarTree);
    }
}