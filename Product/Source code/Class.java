/*Description:
 * represents a class
 * holding its name and its students
 */

import java.util.LinkedList;

public class Class
{
    private String name;
    /*linked list used for efficiency in adding and deleting students
     *accessing a student by index will never be used anyway
     *no member variable is used to count the number of students
     *because in that case user-defined functions
     *for adding and deleting students would be needed
     *the member function of link list, size() can be used instead
     */
    private LinkedList<Student> students;
    
    Class(){}
    
    Class(String name)
    {
        this.name=name;
        this.students=new LinkedList<Student>();
    }
    
    Class(String name, LinkedList<Student> students)
    {
        this.name=name;
        this.students=students;
    }
    
    public String getName(){return name;}
    
    public LinkedList<Student> getStudents(){return students;}
    
    public void setName(String name){this.name=name;}
    
    public void setStudents(LinkedList<Student> students){this.students=students;}
}