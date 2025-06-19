public class Instructor extends User {
   public Instructor(int id, String username, String name, String password) {
        super(id, username, name, password, "instructor");
    }
    @Override
    public String toString() {
        return getName();
    }
}


