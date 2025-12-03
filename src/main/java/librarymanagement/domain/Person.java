package librarymanagement.domain;

public abstract class Person {
    protected String name;
    protected String email;
    protected String password;

    public Person(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    protected String getPassword() { return password; }
}