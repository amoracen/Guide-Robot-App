package fau.amoracen.guiderobot.data;

/**
 * Class to represent the user's information
 */
public class UserInfo {
    private String firstName;
    private String lastName;
    private String heightFeet;
    private String heightInches;

    /**
     * Default Constructor
     */
    public UserInfo() {

    }
    /**
     * Parameterized constructor
     */
    public UserInfo(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Parameterized constructor
     */
    public UserInfo(String firstName, String lastName, String heightFeet, String heightInches) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getHeightFeet() {
        return heightFeet;
    }

    public void setHeightFeet(String heightFeet) {
        this.heightFeet = heightFeet;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(String heightInches) {
        this.heightInches = heightInches;
    }
}
