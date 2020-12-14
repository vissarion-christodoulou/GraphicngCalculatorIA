/*Description:
 * responsible for doing validation of names and email addresses
 */

import java.util.regex.*;

public class Validate
{
    //check if a given string is a name
    public static boolean isName(String name)
    {
        if(name==null || name.length()==0)
            return false;

        if(Character.isLowerCase(name.charAt(0)))
            return false;

        for(int c=1; c<name.length(); c++)
        {
            if(Character.isUpperCase(name.charAt(c)))
                return false;
        }

        return true;
    }

    //check if a given string is an email
    public static boolean isEmail(String email)
    {
        String validEmail = "^[a-zA-z0-9_+&*-]+(?:\\."+"[a-zA-Z0-9_+&*-]+)*@"
            +"(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(validEmail);
        if(email == null)
            return false;

        return pat.matcher(email).matches();
    }
}