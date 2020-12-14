/*Description:
 * responsible for holding the functions entered by the user
 * and their range
 */

import org.mariuszgromada.math.mxparser.*;
import org.mariuszgromada.math.mxparser.mathcollection.*;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.*;

public class RangedFunction
{
    private Function function;
    private double lower;
    private double upper;
    
    RangedFunction(){}
    
    RangedFunction(Function function, double lower, double upper)
    {
        this.function = function;
        this.lower = lower;
        this.upper = upper;
    }
    
    RangedFunction(RangedFunction rf)
    {
        this.setFunction(rf.getFunction());
        this.setLower(rf.getLower());
        this.setUpper(rf.getUpper());
    }
    
    public Function getFunction(){return function;}
    
    public double getLower(){return lower;}
    
    public double getUpper(){return upper;}
    
    public void setFunction(Function function){this.function=function;}
    
    public void setLower(double lower){this.lower=lower;}
    
    public void setUpper(double upper){this.upper=upper;}
}
