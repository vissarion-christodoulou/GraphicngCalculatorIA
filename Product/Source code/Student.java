/*Description:
 * represents a student
 * holding their name and email address
 */

public class Student
{
    private String name=null;
    private String emailAddress=null;
    
    Student(){}
    
    Student(String name, String emailAddress)
    {
        this.name=name;
        this.emailAddress = emailAddress;
    }
    
    public String getName(){return name;}
    
    public String getEmailAddress(){return emailAddress;}
    
    public void setName(String name){this.name=name;}
    
    public void setEmailAddress(String emailAddress){this.emailAddress=emailAddress;}
}