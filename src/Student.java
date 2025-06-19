public class Student extends User {
   public Student(int id, String username, String name, String password) {
        super(id, username, name, password, "student");  // Rol burada sabit belirleniyor
    }
    @Override
    public String toString() {
        return getName(); // veya getUsername(),  hangisi lazımsa
    }
}


