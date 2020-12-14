/*Description:
 * Main class of the program
 * responsible for the whole GUI and user input
 * coordinates function of other classes
 * reads from and writes to file
 * creates and emails pdf file
 */

/*Limitations:
 * Only works for continuous functions
 * Get around: break non-continuous functions into smaller ones
 * Possibly small floating-precision errors
 * which may however lead to large problems ie in roots of function
 * if a huge range or a function with too many turning points is entered
 */

import java.io.*;
import java.math.*;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;
import java.util.*;
import javafx.application.*;
import javafx.embed.swing.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.mariuszgromada.math.mxparser.*;


public class Main extends Application//to run javafx thread
{
    /*linked list used for efficiency in adding and deleting classes
     *indexing won't be used anyway
     */
    private LinkedList<Class> classes = null;

    /*many different stages used for clarity
     *as to which window should be used
     */
    private Stage stgInit = null;
    private Stage stgClasses = null;
    private Stage stgAddClass = null;
    private Stage stgStudents = null;
    private Stage stgAddStudent = null;
    private Stage stgError = null;
    private Stage stgChooseClass = null;
    private Stage stgFunctions = null;
    private Stage stgGraph = null;
    private Stage stgChooseFunction = null;
    private Stage stgChoose2ndFunction=null;
    private Stage stgGetX=null;

    private String currentClass = null;
    private String prevFileName = "";
    private String curFileName = "";

    public static void main(String[] args)
    {
        launch(args);
    }

    /*this function runs in the beggining of the program
     *it displays the initial window
     *which contains a label, an image and two buttons
     *it also retrieves all the data that the program needs
     *and places it in variables available to the whole program
     */
    @Override public void start(Stage dumpStg)
    {
        readClasses();

        Text txtWelcome = new Text("Welcome!");
        txtWelcome.setFont(new Font(20));

        HBox hbxWelcome = new HBox();
        hbxWelcome.getChildren().add(txtWelcome);
        hbxWelcome.setAlignment(Pos.BASELINE_CENTER);

        Image imgMaths = new Image("Maths.jpg");
        ImageView imgvMaths = new ImageView(imgMaths);

        Button btnClasses = new Button("Classes");
        btnClasses.setMinWidth(166);
        btnClasses.setOnAction(e->btnClassesClicked());

        Button btnEmail = new Button("Email");
        btnEmail.setMinWidth(166);
        btnEmail.setOnAction(e->chooseClass(true));

        Button btnSession = new Button("New Session");
        btnSession.setMinWidth(166);
        btnSession.setOnAction(e->chooseClass(false));

        HBox hbxInitBtns = new HBox();
        hbxInitBtns.getChildren().addAll(btnClasses, btnEmail, btnSession);

        VBox vbxInit = new VBox();
        vbxInit.getChildren().addAll(hbxWelcome, imgvMaths, hbxInitBtns);

        stgInit = new Stage();
        Scene scnInit = new Scene(vbxInit);
        stgInit.setScene(scnInit);
        stgInit.setTitle("Welcome page");
        stgInit.resizableProperty().setValue(false);
        stgInit.show();
    }

    //read classes from file
    public void readClasses()
    {
        classes = new LinkedList<Class>();

        try
        {
            //file where classes are saved
            BufferedReader file = new BufferedReader(new FileReader("Classes.txt"));
            int numberOfClasses = Integer.parseInt(file.readLine());
            for (int i=0; i<numberOfClasses; i++)//read the details for each class
            {
                String name = file.readLine();
                LinkedList<Student> ls = new LinkedList<Student>();
                int numberOfStudents=Integer.parseInt(file.readLine());
                for (int j=0; j<numberOfStudents; j++)
                {
                    String sname = file.readLine();
                    String email = file.readLine();
                    ls.add(new Student(sname,email));
                }
                classes.add(new Class(name, ls));
            }
            file.close();
        }
        catch(Exception e){return;}
    }

    //show classes and allow editing
    public void btnClassesClicked()
    {
        stgInit.hide();
        VBox vbxClasses = new VBox();

        /*for each class have the program display its name, and buttons to go
         *to members list, and to delete or rename the class
         */
        /*iterating through linked list sequentailly
         *is just as efficient as doing so in an array
         */
        for(Class cur:classes)
        {
            HBox hc1 = new HBox();
            HBox hc2 = new HBox();
            HBox hc3 = new HBox();//to leave space between the text and the buttons
            Text txtClassName = new Text(cur.getName());
            txtClassName.setFont(new Font(16));
            hc1.getChildren().add(txtClassName);
            hc1.setMinWidth(300);
            Button btnStudents = new Button("See students");
            btnStudents.setOnAction(e->btnStudentsClicked(cur.getName()));
            Button btnDelete = new Button("Delete class");
            btnDelete.setOnAction(e->btnDeleteClassClicked(cur.getName()));
            hc2.getChildren().addAll(btnStudents, btnDelete);
            hc3.getChildren().addAll(hc1, hc2);
            vbxClasses.getChildren().add(hc3);
        }

        ScrollPane spClasses = new ScrollPane();
        spClasses.setContent(vbxClasses);//in case there are too many classes
        spClasses.setMaxHeight(500);

        Button btnAdd = new Button("Add class");
        btnAdd.setOnAction(e->btnAddClicked());

        Button btnDone = new Button("Done");
        btnDone.setOnAction(e->{saveClasses();stgInit.show();stgClasses.hide();});

        HBox hbxBtns = new HBox();
        hbxBtns.getChildren().addAll(btnAdd, btnDone);
        hbxBtns.setAlignment(Pos.BASELINE_RIGHT);

        VBox vbxClassesScene = new VBox();
        vbxClassesScene.getChildren().addAll(spClasses, hbxBtns);

        stgClasses = new Stage();
        stgClasses.resizableProperty().setValue(false);
        /*if x button clicked or hotkey alt-f4 pressed
         * save changes and return to initial screen
         * just as if done button was clicked
         */
        stgClasses.setOnCloseRequest(e->{saveClasses();stgInit.show();stgClasses.hide();});
        Scene scnClasses = new Scene(vbxClassesScene);
        stgClasses.setScene(scnClasses);
        stgClasses.setTitle("Classes");
        stgClasses.show();
    }

    /*choose class either to send a file from the computer
     *or to start a new session(graphs-saving to pdf-sending via email)
     *the value of the boolean isEmail determines which line will be followed
     */
    public void chooseClass(boolean isEmail)
    {
        Text txtChooseClass = new Text("Choose class");

        HBox hb = new HBox();

        ComboBox cmbClasses = new ComboBox();
        for(Class cl: classes)
        {
            cmbClasses.getItems().add(cl.getName());
        }
        cmbClasses.setValue(classes.getFirst().getName());

        Button btnDone = new Button("Done");
        if(!isEmail)
            btnDone.setOnAction(e->
                {
                    currentClass = cmbClasses.getValue().toString();
                    stgChooseClass.hide();
                    prevFileName="";
                    curFileName="";
                    getFunctions();
                });
        else
        {
            btnDone.setOnAction(e->
                {
                    currentClass = cmbClasses.getValue().toString();
                    stgChooseClass.hide();
                    stgInit.show();
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(stgInit);
                    final String userEmail = "computerscienceia7@gmail.com";
                    final String userPassword = "compScience100%";
                    Properties p = System.getProperties();
                    p.setProperty("mail.smtp.host", "smtp.gmail.com");
                    p.put("mail.smtp.auth", "true");
                    p.put("mail.smtp.starttls.enable", "true");
                    p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
                    Session s = Session.getDefaultInstance(p,
                                                           new javax.mail.Authenticator()
                                                           {
                                                               protected PasswordAuthentication getPasswordAuthentication()
                                                               {
                                                                   return new PasswordAuthentication(userEmail, userPassword);
                                                               }
                                                           });
                    try
                    {
                        for(Class cur:classes)
                        {
                            if(cur.getName().equals(currentClass))
                            {
                                MimeMessage m = new MimeMessage(s);
                                m.setFrom(new InternetAddress(userEmail));
                                for(Student st:cur.getStudents())
                                    m.addRecipient(Message.RecipientType.TO, new InternetAddress(st.getEmailAddress()));
                                m.setSubject("File");
                                
                                BodyPart mB = new MimeBodyPart();
                                mB.setText("Automated email with chosen file attached");
                                MimeBodyPart mB2 = new MimeBodyPart();
                                mB2.attachFile(file);
                                
                                Multipart mult = new MimeMultipart();
                                mult.addBodyPart(mB);
                                mult.addBodyPart(mB2);
                                
                                m.setContent(mult);
                                Transport.send(m);
                                break;
                            }
                        }
                    }
                    catch(Exception ex){}
                });
        }

        hb.getChildren().addAll(cmbClasses, btnDone);

        stgInit.hide();
        stgChooseClass = new Stage();
        stgChooseClass.resizableProperty().setValue(false);
        /*if x button clicked or hotkey alt-f4 pressed
         * save changes and return to initial screen
         * just as if done button was clicked
         */
        stgChooseClass.setOnCloseRequest(e->{stgChooseClass.hide(); stgInit.show();});
        Scene scnChooseClass = new Scene(hb);
        stgChooseClass.setScene(scnChooseClass);
        stgChooseClass.setTitle("Choose class");
        stgChooseClass.show();
    }

    //get functions to be graphed from the user
    public void getFunctions()
    {
        PDDocument pdf = new PDDocument();
        VBox vbxFunctions = new VBox();
        HBox hbxButtons = new HBox();
        Button btnAddFunction = new Button("Add function");
        btnAddFunction.setOnAction(e->btnAddFunctionClicked(vbxFunctions));
        Button btnExe = new Button("Execute");
        btnExe.setOnAction(e->btnExeClicked(vbxFunctions, pdf));
        hbxButtons.getChildren().addAll(btnAddFunction, btnExe);
        hbxButtons.setAlignment(Pos.BASELINE_RIGHT);
        vbxFunctions.getChildren().addAll(hbxButtons);

        stgFunctions = new Stage();
        stgFunctions.resizableProperty().setValue(false);
        //if x button clicked or hotkey alt-f4 pressed return to initial screen
        stgFunctions.setOnCloseRequest(e->{curFunc=0;email();try{pdf.close();}catch(Exception ex){}stgFunctions.hide(); stgInit.show();});

        Scene scnFunctions = new Scene(vbxFunctions);
        stgFunctions.setScene(scnFunctions);
        stgFunctions.setTitle("Functions");
        stgFunctions.show();
    }

    private int curFunc = 0;
    //allow user to add one more function
    public void btnAddFunctionClicked(VBox vbxFunctions)
    {
        HBox hbxFunction = new HBox();
        Text txtFx = new Text("f" + curFunc + "(x) = ");
        TextField txtfFunction = new TextField();
        Text txtRange = new Text("in range");
        TextField txtfLower = new TextField();
        Text txtTo = new Text("to");
        TextField txtfUpper = new TextField();
        Button btnDeleteFunction = new Button("Delete function");
        int deleteIndex = curFunc;
        btnDeleteFunction.setOnAction(e->btnDeleteFunctionClicked(vbxFunctions,deleteIndex));
        hbxFunction.getChildren().addAll(txtFx, txtfFunction, txtRange, txtfLower, txtTo, txtfUpper, btnDeleteFunction);
        vbxFunctions.getChildren().add(curFunc, hbxFunction);
        curFunc++;
        stgFunctions.sizeToScene();
    }

    //allow user to delete a certain function
    public void btnDeleteFunctionClicked(VBox vbxFunctions, int deleteIndex)
    {
        vbxFunctions.getChildren().remove(deleteIndex);
        for(int i=deleteIndex; i<vbxFunctions.getChildren().size()-1; i++)
        {
            HBox hbxFunction = (HBox)vbxFunctions.getChildren().get(i);
            Text txtFx = (Text)hbxFunction.getChildren().get(0);
            txtFx.setText("f" + i + "(x) = ");
            Button btnDeleteFunction = (Button)hbxFunction.getChildren().get(6);
            final int j=i;
            btnDeleteFunction.setOnAction(e->btnDeleteFunctionClicked(vbxFunctions, j));
        }
        curFunc--;
        stgFunctions.sizeToScene();
    }

    //Check the validity of the given functions before sketching the graph
    public void btnExeClicked(VBox vbxFunctions, PDDocument pdf)
    {
        if(vbxFunctions.getChildren().size() == 1)
        {
            throwError("No functions entered", stgFunctions);
            return;
        }
        
        RangedFunction[] rfunctions = new RangedFunction[vbxFunctions.getChildren().size()-1];
        for(int i=0; i<vbxFunctions.getChildren().size()-1; i++)
            rfunctions[i] = new RangedFunction();
        /*Function[] functions = new Function[vbxFunctions.getChildren().size()-1];
        double[] lower = new double[vbxFunctions.getChildren().size()-1];
        double[] upper = new double[vbxFunctions.getChildren().size()-1];*/
        for(int i=0; i<vbxFunctions.getChildren().size()-1; i++)
        {
            HBox hbxFunction = (HBox)vbxFunctions.getChildren().get(i);
            TextField txtfFunction = (TextField)hbxFunction.getChildren().get(1);
            String func = txtfFunction.getText();
            Function f = new Function("f" + i + "(x)=" + func);
            f.checkSyntax();
            if(f.getErrorMessage().substring(f.getErrorMessage().length()-19).equals("errors were found.\n"))
            {
                throwError("Invalid function syntax, f" + i, stgFunctions);
                return;
            }
            if(f.getFunctionExpressionString().equals(""))
            {
                throwError("Invalid function syntax, f" + i, stgFunctions);
                return;
            }

            rfunctions[i].setFunction(f);
            TextField txtfLower = (TextField)hbxFunction.getChildren().get(3);
            TextField txtfUpper = (TextField)hbxFunction.getChildren().get(5);
            String low = txtfLower.getText();
            String high = txtfUpper.getText();
            try
            {
                rfunctions[i].setLower(Double.parseDouble(low));
                rfunctions[i].setUpper(Double.parseDouble(high));
            }
            catch(Exception e)
            {
                throwError("Invalid range, f" + i, stgFunctions);
                return;
            }

            if(rfunctions[i].getLower() > rfunctions[i].getUpper())
            {
                throwError("Invalid range, f" + i, stgFunctions);
                return;
            }
        }

        execute(rfunctions, pdf);
    }

    //plot the functions and allow any kind of calculations on them
    public void execute(RangedFunction[] rfunctions, PDDocument pdf)
    {
        stgFunctions.hide();

        VBox vbxGraph = new VBox();
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        LineChart<Number, Number> graph = new LineChart<Number, Number>(xAxis, yAxis);
        graph.setCreateSymbols(false);

        XYChart.Series[] series = new XYChart.Series[rfunctions.length];
        for(int i=0; i<series.length; i++)
        {
            series[i] = new XYChart.Series();
            series[i].setName("y=f" + i + "=" + rfunctions[i].getFunction().getFunctionExpressionString());
            boolean num=false;
            for (double d=rfunctions[i].getLower(); d<=rfunctions[i].getUpper(); d+=(rfunctions[i].getUpper()-rfunctions[i].getLower())/2000)
            {
                double temp=rfunctions[i].getFunction().calculate(d);
                if (!Double.isNaN(temp))
                {
                    if(!num)
                    {
                        rfunctions[i].setLower(d);
                        num=true;
                    }
                    series[i].getData().add(new XYChart.Data(d, temp));
                }
                else if(num)
                {
                    rfunctions[i].setUpper(((rfunctions[i].getUpper()-rfunctions[i].getLower())/2000));
                }
            }
            graph.getData().add(series[i]);
        }

        HBox hbxButtons1 = new HBox();
        HBox hbxButtons2 = new HBox();
        Button btnMax = new Button("Max");
        btnMax.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Max"));
        Button btnMin = new Button("Min");
        btnMin.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Min"));
        Button btnCalcY = new Button("Calculate y");
        btnCalcY.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Calculate y"));
        Button btnRoots = new Button("Roots");
        btnRoots.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Roots"));
        Button btnYIntercept = new Button("y-intercept");
        btnYIntercept.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "YIntercept"));
        Button btnDerivative = new Button("Derivative");
        btnDerivative.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Derivative"));
        Button btnAreaUnder = new Button("Area under the graph");
        btnAreaUnder.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "AreaUnder"));
        Button btnAreaBetweenX = new Button("Area between graph and x-axis");
        btnAreaBetweenX.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "AreaBetween"));
        Button btnVolume = new Button("Volume of revolution");
        btnVolume.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Volume"));
        Button btnIntersection = new Button("Intersection");
        btnIntersection.setOnAction(e->chooseFunction(vbxGraph, rfunctions, "Intersection"));
        ScrollPane spGraph = new ScrollPane();
        Button btnSave = new Button("Save");
        btnSave.setOnAction(e->btnSaveFunctionClicked(spGraph, pdf));
        hbxButtons1.getChildren().addAll(btnMax, btnMin, btnCalcY, btnRoots, btnYIntercept, btnDerivative);
        hbxButtons2.getChildren().addAll(btnAreaUnder, btnAreaBetweenX, btnVolume, btnSave);

        if(rfunctions.length > 1)
            hbxButtons2.getChildren().add(3, btnIntersection);

        vbxGraph.getChildren().addAll(graph, hbxButtons1, hbxButtons2);
        spGraph.setContent(vbxGraph);
        spGraph.setMaxHeight(700);

        stgGraph = new Stage();
        stgGraph.resizableProperty().setValue(false);
        //if x button clicked or hotkey alt-f4 pressed return to initial screen
        stgGraph.setOnCloseRequest(e->{stgGraph.hide(); stgFunctions.show();});
        Scene scnGraph = new Scene(spGraph);
        stgGraph.setScene(scnGraph);
        stgGraph.setTitle("Graph");
        stgGraph.show();
    }

    //save current graph and information in pdf
    public void btnSaveFunctionClicked(ScrollPane spGraph, PDDocument pdf)
    {
        LineChart<Number, Number> temp = (LineChart)(((VBox)(spGraph.getContent())).getChildren().get(0));
        WritableImage wrtImg = temp.snapshot(new SnapshotParameters(), null);
        File fTemp = new File("graph.png");
        String fName="";
        try
        {
            ImageIO.write(SwingFXUtils.fromFXImage(wrtImg, null), "png", fTemp);
            PDImageXObject pdImgTemp = PDImageXObject.createFromFile("graph.png", pdf);
            PDPage graphPage = new PDPage(new PDRectangle(pdImgTemp.getWidth(), pdImgTemp.getHeight()));
            PDPageContentStream pdCont = new PDPageContentStream(pdf, graphPage);
            pdCont.drawImage(pdImgTemp, 0, 0);
            pdCont.close();
            pdf.addPage(graphPage);
            fTemp.delete();
            int counter=1;
            while(counter < ((VBox)(spGraph.getContent())).getChildren().size()-2)
            {
                PDPage textPage = new PDPage(new PDRectangle(pdImgTemp.getWidth(), pdImgTemp.getHeight()));
                pdf.addPage(textPage);
                PDPageContentStream textContent = new PDPageContentStream(pdf, textPage);
                textContent.beginText();
                PDFont textFont = PDType1Font.HELVETICA_BOLD;
                textContent.setFont(textFont, 12);
                int totalYSpace=20;
                textContent.moveTextPositionByAmount(10, pdImgTemp.getHeight()-20);

                while(totalYSpace <= pdImgTemp.getHeight()-20 && counter < ((VBox)(spGraph.getContent())).getChildren().size()-2)
                {
                    String str = ((Text)(((VBox)(spGraph.getContent())).getChildren().get(counter))).getText();
                    String[] toPdf = new String[str.length()/68+3];
                    int cur=0;
                    while(true)
                    {
                        if(str.length() <= 68)
                        {
                            if(totalYSpace>=pdImgTemp.getHeight()-20)
                            {
                                textContent.endText();
                                textContent.close();
                                textPage = new PDPage(new PDRectangle(pdImgTemp.getWidth(), pdImgTemp.getHeight()));               
                                pdf.addPage(textPage);
                                textContent = new PDPageContentStream(pdf, textPage);
                                textContent.beginText();
                                textContent.setFont(textFont, 12);
                                totalYSpace=20;
                                textContent.moveTextPositionByAmount(10, pdImgTemp.getHeight()-20);
                            }
                            textContent.drawString(str);
                            totalYSpace+=20;
                            textContent.moveTextPositionByAmount(0, -20);
                            str="";
                            break;
                        }
                        boolean whitespace=false;
                        for(int i=68; i>0; i--)
                        {
                            if(Character.isWhitespace(str.charAt(i)))
                            {
                                if(totalYSpace>=pdImgTemp.getHeight()-20)
                                {
                                    textContent.endText();
                                    textContent.close();
                                    textPage = new PDPage(new PDRectangle(pdImgTemp.getWidth(), pdImgTemp.getHeight()));
                                    pdf.addPage(textPage);
                                    textContent = new PDPageContentStream(pdf, textPage);
                                    textContent.beginText();
                                    textContent.setFont(textFont, 12);
                                    totalYSpace=20;
                                    textContent.moveTextPositionByAmount(10, pdImgTemp.getHeight()-20);
                                }
                                String substring = str.substring(0,i+1);
                                textContent.drawString(substring);
                                totalYSpace+=20;
                                textContent.moveTextPositionByAmount(0, -20);
                                str=str.substring(i+1, str.length());
                                break;
                            }
                        }
                        if(!whitespace)
                        {
                            if(totalYSpace>=pdImgTemp.getHeight()-20)
                            {
                                textContent.endText();
                                textContent.close();
                                textPage = new PDPage(new PDRectangle(pdImgTemp.getWidth(), pdImgTemp.getHeight()));
                                pdf.addPage(textPage);
                                textContent = new PDPageContentStream(pdf, textPage);
                                textContent.beginText();
                                textContent.setFont(textFont, 12);
                                totalYSpace=20;
                                textContent.moveTextPositionByAmount(10, pdImgTemp.getHeight()-20);
                            }
                            String substring = str.substring(0,68);
                            textContent.drawString(substring);
                            totalYSpace+=20;
                            textContent.moveTextPositionByAmount(0, -20);
                            str=str.substring(68, str.length());
                        }
                    }

                    counter++;
                }
                textContent.endText();
                textContent.close();
            }
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy.MM.dd HH.mm.ss");  
            LocalDateTime dateTime = LocalDateTime.now();
            fName = currentClass + " " + f.format(dateTime) + ".pdf";
            prevFileName = curFileName;
            if(!prevFileName.equals(""))
            {
                File del = new File(prevFileName);
                del.delete();
            }
            curFileName = fName;
            pdf.save(curFileName);
        }
        catch(Exception e){}
    }
    
    //send pdf created to students of currentClass
    public void email()
    {
        if(curFileName.equals(""))
            return;
        File file = new File(curFileName);
        final String userEmail = "computerscienceia7@gmail.com";
        final String userPassword = "compScience100%";
        Properties p = System.getProperties();
        p.setProperty("mail.smtp.host", "smtp.gmail.com");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        Session s = Session.getDefaultInstance(p,
                                               new javax.mail.Authenticator()
                                               {
                                                   protected PasswordAuthentication getPasswordAuthentication()
                                                   {
                                                       return new PasswordAuthentication(userEmail, userPassword);
                                                   }
                                               });
        try
        {
            for(Class cur:classes)
            {
                if(cur.getName().equals(currentClass))
                {
                    MimeMessage m = new MimeMessage(s);
                    m.setFrom(new InternetAddress(userEmail));
                    for(Student st:cur.getStudents())
                        m.addRecipient(Message.RecipientType.TO, new InternetAddress(st.getEmailAddress()));
                    m.setSubject("Class diagrams");
                    
                    BodyPart mB = new MimeBodyPart();
                    mB.setText("Automated email with class diagrams and corresponding data");
                    MimeBodyPart mB2 = new MimeBodyPart();
                    mB2.attachFile(file);
                    
                    Multipart mult = new MimeMultipart();
                    mult.addBodyPart(mB);
                    mult.addBodyPart(mB2);
                    
                    m.setContent(mult);
                    Transport.send(m);
                    break;
                }
            }
        }
        catch(Exception ex){}
    }

    //choose which function the calculations will be done for
    public void chooseFunction(VBox vbxGraph, RangedFunction[] rfunctions, String process)
    {
        Text txtChooseFunction = new Text("Choose function");

        HBox hb = new HBox();

        ComboBox cmbFunctions = new ComboBox();
        for(int i=0; i<rfunctions.length; i++)
        {
            cmbFunctions.getItems().add("f" + i + "(x)");
        }
        cmbFunctions.setValue("f0(x)");

        Button btnDone = new Button("Done");
        btnDone.setOnAction(e->
            {
                int ind=cmbFunctions.getSelectionModel().getSelectedIndex();
                RangedFunction rf = new RangedFunction(rfunctions[ind]);
                if(process.equals("Max"))
                    btnMaxClicked(vbxGraph, rf);
                else if(process.equals("Min"))
                    btnMinClicked(vbxGraph, rf);
                else if(process.equals("Calculate y"))
                    getX(vbxGraph, rf, process);
                else if(process.equals("Roots"))
                    btnRootsClicked(vbxGraph, rf);
                else if(process.equals("YIntercept")) 
                    btnYInterceptClicked(vbxGraph, rf);
                else if(process.equals("Derivative"))
                    getX(vbxGraph, rf, process);
                else if(process.equals("AreaUnder"))
                    btnAreaUnderClicked(vbxGraph, rf);
                else if(process.equals("AreaBetween"))
                    btnAreaBetweenClicked(vbxGraph, rf);
                else if(process.equals("Volume"))
                    btnVolumeClicked(vbxGraph, rf);
                else if(process.equals("Intersection"))
                    choose2ndFunction(vbxGraph, rfunctions,ind);
            });

        hb.getChildren().addAll(cmbFunctions, btnDone);

        stgGraph.hide();
        stgChooseFunction = new Stage();
        stgChooseFunction.resizableProperty().setValue(false);
        //if x button clicked or hotkey alt-f4 pressed
        stgChooseFunction.setOnCloseRequest(e->{stgChooseFunction.hide(); stgGraph.show();});
        Scene scnChooseFunction = new Scene(hb);
        stgChooseFunction.setScene(scnChooseFunction);
        stgChooseFunction.setTitle("Choose function for " + process);
        stgChooseFunction.show();
    }

    /*
     *choose a second function if we are looking for the intersection
     *of the graph of two functions
     */
    public void choose2ndFunction(VBox vbxGraph, RangedFunction[] rfunctions, int index)
    {
        Text txtChoose2ndFunction = new Text("Choose second function");

        HBox hb = new HBox();

        ComboBox cmbFunctions = new ComboBox();
        for(int i=0; i<rfunctions.length; i++)
        {
            if(i!=index)
                cmbFunctions.getItems().add("f" + i + "(x)");
        }
        if(index!=0)
            cmbFunctions.setValue("f0(x)");
        else
            cmbFunctions.setValue("f1(x)");

        Button btnDone = new Button("Done");
        btnDone.setOnAction(e->
            {
                int ind2=cmbFunctions.getSelectionModel().getSelectedIndex();
                if (ind2>=index) 
                    ind2++;
                btnIntersectionClicked(vbxGraph, rfunctions[index], rfunctions[ind2]);
            });

        hb.getChildren().addAll(txtChoose2ndFunction, cmbFunctions, btnDone);
        stgChooseFunction.hide();
        stgChoose2ndFunction = new Stage();
        stgChoose2ndFunction.resizableProperty().setValue(false);
        //if x button clicked or hotkey alt-f4 pressed
        stgChoose2ndFunction.setOnCloseRequest(e->{stgChoose2ndFunction.hide(); stgGraph.show();});
        Scene scnChoose2ndFunction = new Scene(hb);
        stgChoose2ndFunction.setScene(scnChoose2ndFunction);
        stgChoose2ndFunction.setTitle("Choose second function for intersection");
        stgChoose2ndFunction.show();

    }

    /*we can calculate the derivative of a function or its y-value, given an x-input
     *this method gets this x-value-input from the user
     */
    public void getX(VBox vbxGraph, RangedFunction rfunction, String process)
    {
        HBox hbxGetX = new HBox();
        Text txtAt = new Text(process + " at x=");
        TextField txtfX = new TextField();
        Button btnDone = new Button("Done");
        btnDone.setOnAction(e->
            {
                if(process.equals("Derivative"))
                    btnDerivativeClicked(vbxGraph, rfunction, txtfX.getText());
                else if(process.equals("Calculate y"))
                    btnCalcYClicked(vbxGraph, rfunction, txtfX.getText());
            });
        hbxGetX.getChildren().addAll(txtAt, txtfX, btnDone);

        stgChooseFunction.hide();
        stgGetX = new Stage();
        stgGetX.resizableProperty().setValue(false);
        //if x button clicked or hotkey alt-f4 pressed
        stgGetX.setOnCloseRequest(e->{stgGetX.hide(); stgChooseFunction.show();});
        Scene scnGetX = new Scene(hbxGetX);
        stgGetX.setScene(scnGetX);
        stgGetX.setTitle("Choose x0 for " + process);
        stgGetX.show();
    }

    //find the maxima of the given function within its range
    public void btnMaxClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double diff = (rfunction.getUpper()-rfunction.getLower())/numberOfPoints;
        double yprev=rfunction.getFunction().calculate(rfunction.getLower());
        double ycur=rfunction.getFunction().calculate(rfunction.getLower()+diff);
        double ynext;
        if (rfunction.getFunction().calculate(rfunction.getLower()) > rfunction.getFunction().calculate(rfunction.getLower()+diff))
        {
            Text txtMax = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has maximum at (" + 
                                    BigDecimal.valueOf(rfunction.getLower()).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                    + BigDecimal.valueOf(rfunction.getFunction().calculate(rfunction.getLower())).setScale
                                    (8, RoundingMode.HALF_UP).doubleValue() +")"); 
            txtMax.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtMax);
        }
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper()-diff; d+=diff)
        {
            ynext=rfunction.getFunction().calculate(d+diff);
            if(ycur > yprev && ycur > ynext)
            {
                double maxY=yprev;
                double maxX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=rfunction.getFunction().calculate(d2);
                    if(temp > maxY)
                    {
                        maxY = temp;
                        maxX = d2;
                    }
                    else break;
                }

                Text txtMax = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has maximum at (" 
                                        + BigDecimal.valueOf(maxX).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                        + BigDecimal.valueOf(maxY).setScale(8, RoundingMode.HALF_UP).doubleValue() +")"); 
                txtMax.setWrappingWidth(width);
                vbxGraph.getChildren().add(1, txtMax);
            }
            yprev=ycur;
            ycur=ynext;
        } 
        if (rfunction.getFunction().calculate(rfunction.getUpper()) > rfunction.getFunction().calculate(rfunction.getUpper()-diff))
        {
            Text txtMax = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has maximum at (" 
                                    + BigDecimal.valueOf(rfunction.getUpper()).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                    + BigDecimal.valueOf(rfunction.getFunction().calculate(rfunction.getUpper())).setScale
                                    (8, RoundingMode.HALF_UP).doubleValue() +")"); 
            txtMax.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtMax);
        }

        stgChooseFunction.hide();
        stgGraph.show();
    }

    //find the minima of the given function within its range
    public void btnMinClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double diff = (rfunction.getUpper()-rfunction.getLower())/numberOfPoints;
        double yprev=rfunction.getFunction().calculate(rfunction.getLower());
        double ycur=rfunction.getFunction().calculate(rfunction.getLower()+diff);
        double ynext;
        if (rfunction.getFunction().calculate(rfunction.getLower()) < rfunction.getFunction().calculate(rfunction.getLower()+diff))
        {
            Text txtMin = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has minimum at (" 
                                    + BigDecimal.valueOf(rfunction.getLower()).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                    + BigDecimal.valueOf(rfunction.getFunction().calculate(rfunction.getLower())).setScale
                                    (8, RoundingMode.HALF_UP).doubleValue() +")"); 
            txtMin.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtMin);
        }
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper()-diff; d+=diff)
        {
            ynext=rfunction.getFunction().calculate(d+diff);
            if(ycur < yprev && ycur < ynext)
            {
                double minY=yprev;
                double minX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=rfunction.getFunction().calculate(d2);
                    if(temp < minY)
                    {
                        minY = temp;
                        minX = d2;
                    }
                    else break;
                }
                Text txtMin = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has minimum at (" 
                                        + BigDecimal.valueOf(minX).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                        + BigDecimal.valueOf(minY).setScale(8, RoundingMode.HALF_UP).doubleValue() +")"); 
                txtMin.setWrappingWidth(width);
                vbxGraph.getChildren().add(1, txtMin);
            }
            yprev=ycur;
            ycur=ynext;
        } 
        if (rfunction.getFunction().calculate(rfunction.getUpper()) < rfunction.getFunction().calculate(rfunction.getUpper()-diff))
        {
            Text txtMin = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has minimum at (" 
                                    + BigDecimal.valueOf(rfunction.getUpper()).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                    + BigDecimal.valueOf(rfunction.getFunction().calculate(rfunction.getUpper())).setScale
                                    (8, RoundingMode.HALF_UP).doubleValue() +")"); 
            txtMin.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtMin);
        }

        stgChooseFunction.hide();
        stgGraph.show();
    }

    //calculate y for a given value of x for a function
    public void btnCalcYClicked(VBox vbxGraph, RangedFunction rfunction, String x0)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();
        double x=0;
        try
        {
            x = Double.parseDouble(x0);
        }
        catch(Exception e)
        {
            throwError("Not valid value for x", stgGetX);
            return;
        }

        if(x<rfunction.getLower() || x>rfunction.getUpper())
        {
            throwError("Not valid value for x", stgGetX);
            return;
        }

        Text txtCalcY = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + ", " + rfunction.getFunction().getFunctionName() 
                                  + "(" + x + ")=" + rfunction.getFunction().calculate(x));
        txtCalcY.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtCalcY);
        stgGetX.hide();
        stgGraph.show();
    }

    //find the y-intercept of a function given within its range
    public void btnYInterceptClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();
        HBox hbxText = new HBox();
        Text txtYIntercept = new Text();
        if (0<rfunction.getLower() || 0>rfunction.getUpper())
            txtYIntercept.setText("y=" + rfunction.getFunction().getFunctionExpressionString() + " has no  y intercept within its range");
        else
            txtYIntercept.setText("y=" + rfunction.getFunction().getFunctionExpressionString() + " intersects the y-axis at (0.0, " 
                                    + BigDecimal.valueOf(rfunction.getFunction().calculate(0)).setScale(8, RoundingMode.HALF_UP).doubleValue()+ ")");

        txtYIntercept.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtYIntercept);
        stgChooseFunction.hide();
        stgGraph.show();
    }

    /*this final static member variable is used in almost all methods
     *the greater the numberOfPoints is, the greater the accuracy
     *in all calculations, but also the more time-consuming
     *the value 20000 was found practically to be achieving a balance 
     *between time efficiency and accuracy in most functions
     *that are usually used
     */
    private final static int numberOfPoints=20000;

    //find the roots of a function given within its range
    public void btnRootsClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        boolean rootFound=false;
        double diff=(rfunction.getUpper()-rfunction.getLower())/numberOfPoints;
        double yprev=rfunction.getFunction().calculate(rfunction.getLower());
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper(); d+=diff)
        {
            double y=rfunction.getFunction().calculate(d);
            if((yprev<0 && y>0) || (yprev>0 && y<0))
            {
                rootFound=true;
                double closestToZero=Math.abs(yprev);
                double x=d-diff;
                for(double d2=d-diff; d2<=d; d2+=diff/numberOfPoints)
                {
                    double temp=rfunction.getFunction().calculate(d2);
                    if(Math.abs(temp)<closestToZero)
                    {
                        closestToZero = Math.abs(temp);
                        x=d2;
                    }
                }
                Text txtRoot = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has root at x=" 
                                        + BigDecimal.valueOf(x).setScale(8, RoundingMode.HALF_UP).doubleValue());
                txtRoot.setWrappingWidth(width);
                vbxGraph.getChildren().add(1, txtRoot);
            }
            yprev=y;
        }

        yprev=rfunction.getFunction().calculate(rfunction.getLower());
        double ycur=rfunction.getFunction().calculate(rfunction.getLower()+diff);
        double ynext;
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper()-diff; d+=diff)
        {
            ynext=rfunction.getFunction().calculate(d+diff);
            if(ycur > yprev && ycur > ynext && ynext<0 && yprev<0)
            {
                double closestY=-yprev;
                double closestX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=Math.abs(rfunction.getFunction().calculate(d2));
                    if(temp < closestY)
                    {
                        closestY = temp;
                        closestX = d2;
                    }
                    else break;
                }
                if(closestY < 1e-5)
                {
                    rootFound=true;
                    Text txtRoot = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has root at x=" 
                                            + BigDecimal.valueOf(closestX).setScale(8, RoundingMode.HALF_UP).doubleValue());
                    txtRoot.setWrappingWidth(width);
                    vbxGraph.getChildren().add(1, txtRoot);
                }
            }
            yprev=ycur;
            ycur=ynext;
        }

        yprev=rfunction.getFunction().calculate(rfunction.getLower());
        ycur=rfunction.getFunction().calculate(rfunction.getLower()+diff);
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper()-diff; d+=diff)
        {
            ynext=rfunction.getFunction().calculate(d+diff);
            if(ycur < yprev && ycur < ynext && ynext>0 && yprev>0)
            {
                double closestY=yprev;
                double closestX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=Math.abs(rfunction.getFunction().calculate(d2));
                    if(temp < closestY)
                    {
                        closestY = temp;
                        closestX = d2;
                    }
                    else break;
                }
                if(closestY < 1e-5)
                {
                    rootFound=true;
                    Text txtRoot = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has root at x=" 
                                            + BigDecimal.valueOf(closestX).setScale(8, RoundingMode.HALF_UP).doubleValue());           
                    txtRoot.setWrappingWidth(width);
                    vbxGraph.getChildren().add(1, txtRoot);
                }
            }
            yprev=ycur;
            ycur=ynext;
        } 

        if(!rootFound)
        {
            Text txtNoRoot = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + " has no roots within its range");
            txtNoRoot.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtNoRoot);
        }

        stgChooseFunction.hide();
        stgGraph.show();
    }

    //find the derivative of a function given at a certain point within its range
    public void btnDerivativeClicked(VBox vbxGraph, RangedFunction rfunction, String x0)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double x=0;
        try
        {
            x = Double.parseDouble(x0);
        }
        catch(Exception e)
        {
            throwError("Not valid value for x", stgGetX);
            return;
        }

        if(x<rfunction.getLower() || x>rfunction.getUpper())
        {
            throwError("Not valid value for x", stgGetX);
            return;
        }

        Text txtDerivative = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + ", " + rfunction.getFunction().getFunctionName() + "'(" 
                                       + x + ")=" + ((rfunction.getFunction().calculate(x+(1e-5))-rfunction.getFunction().calculate(x-(1e-5)))/(2e-5)));
        txtDerivative.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtDerivative);
        stgGetX.hide();
        stgGraph.show();
    }

    //find the area under the graph of a given function and above the x-axis
    public void btnAreaUnderClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double area=0;
        double diff=(rfunction.getUpper()-rfunction.getLower())/(numberOfPoints*5);
        double prev=rfunction.getFunction().calculate(rfunction.getLower());
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper(); d+=diff)
        {
            double temp=rfunction.getFunction().calculate(d);
            area += (prev+temp)*diff/2;
            prev=temp;
        }
        Text txtAreaUnder = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + ", area under graph= " + area);
        txtAreaUnder.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtAreaUnder);
        stgChooseFunction.hide();
        stgGraph.show();
    }

    /*find the area between the graph of a given function and the x-axis
     *this is different from what the previous method does
     *as it only takes positive values 
     */
    public void btnAreaBetweenClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double area=0;
        double diff=(rfunction.getUpper()-rfunction.getLower())/(numberOfPoints*5);
        double prev=Math.abs(rfunction.getFunction().calculate(rfunction.getLower()));
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper(); d+=diff)
        {
            double temp=Math.abs(rfunction.getFunction().calculate(d));
            area += (prev+temp)*diff/2;
            prev=temp;
        }
        Text txtAreaBetween = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + ", area between graph and x-axis= " + area);
        txtAreaBetween.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtAreaBetween);
        stgChooseFunction.hide();
        stgGraph.show();
    }

    //find the volume of revolution of the graph of a given function
    public void btnVolumeClicked(VBox vbxGraph, RangedFunction rfunction)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        double volume=0;
        double diff=(rfunction.getUpper()-rfunction.getLower())/(numberOfPoints*5);
        double prev=rfunction.getFunction().calculate(rfunction.getLower());
        for(double d=rfunction.getLower()+diff; d<=rfunction.getUpper(); d+=diff)
        {
            double temp=rfunction.getFunction().calculate(d);
            volume += Math.PI*(prev*prev+temp*temp)*diff/2;
            prev=temp;
        }
        Text txtVolume = new Text("y=" + rfunction.getFunction().getFunctionExpressionString() + ", volume of revolution= " + volume);
        txtVolume.setWrappingWidth(width);
        vbxGraph.getChildren().add(1, txtVolume);
        stgChooseFunction.hide();
        stgGraph.show();
    }

    //find the intersection of the graph of two functions
    public void btnIntersectionClicked(VBox vbxGraph, RangedFunction f1, RangedFunction f2)
    {
        double width = ((LineChart)(vbxGraph.getChildren().get(0))).getWidth();

        stgChoose2ndFunction.hide();
        double lower = (f1.getLower()>f2.getLower()) ? f1.getLower() : f2.getLower();
        double upper = (f1.getUpper()<f2.getUpper()) ? f1.getUpper() : f2.getUpper();
        Function function = new Function("g(x)="+f1.getFunction().getFunctionExpressionString() + "-(" + f2.getFunction().getFunctionExpressionString() + ")");

        boolean rootFound=false;
        double diff=(upper-lower)/numberOfPoints;
        double yprev=function.calculate(lower);
        for(double d=lower+diff; d<=upper; d+=diff)
        {
            double y=function.calculate(d);
            if((yprev<0 && y>0) || (yprev>0 && y<0))
            {
                rootFound=true;
                double closestToZero=Math.abs(yprev);
                double x=d-diff;
                for(double d2=d-diff; d2<=d; d2+=diff/numberOfPoints)
                {
                    double temp=function.calculate(d2);
                    if(Math.abs(temp)<closestToZero)
                    {
                        closestToZero = Math.abs(temp);
                        x=d2;
                    }
                }
                Text txtRoot = new Text(f1.getFunction().getFunctionName() + " and " + f2.getFunction().getFunctionName() + " intersect at (" 
                                        + BigDecimal.valueOf(x).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                        + BigDecimal.valueOf(f1.getFunction().calculate(x)).setScale(8, RoundingMode.HALF_UP).doubleValue() + ")");
                txtRoot.setWrappingWidth(width);
                vbxGraph.getChildren().add(1, txtRoot);
            }
            yprev=y;
        }

        yprev=function.calculate(lower);
        double ycur=function.calculate(lower+diff);
        double ynext;
        for(double d=lower+diff; d<=upper-diff; d+=diff)
        {
            ynext=function.calculate(d+diff);
            if(ycur > yprev && ycur > ynext && ynext<0 && yprev<0)
            {
                double closestY=-yprev;
                double closestX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=Math.abs(function.calculate(d2));
                    if(temp < closestY)
                    {
                        closestY = temp;
                        closestX = d2;
                    }
                    else break;
                }
                if(closestY < 1e-5)
                {
                    rootFound=true;
                    Text txtRoot = new Text(f1.getFunction().getFunctionName() + " and " + f2.getFunction().getFunctionName() + " intersect at (" 
                                            + BigDecimal.valueOf(closestX).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                            + BigDecimal.valueOf(f1.getFunction().calculate(closestX)).setScale
                                            (8, RoundingMode.HALF_UP).doubleValue() + ")" );           
                    txtRoot.setWrappingWidth(width);
                    vbxGraph.getChildren().add(1, txtRoot);
                }
            }
            yprev=ycur;
            ycur=ynext;
        }

        yprev=function.calculate(lower);
        ycur=function.calculate(lower+diff);
        for(double d=lower+diff; d<=upper-diff; d+=diff)
        {
            ynext=function.calculate(d+diff);
            if(ycur < yprev && ycur < ynext && ynext>0 && yprev>0)
            {
                double closestY=yprev;
                double closestX=d-diff;
                for(double d2=d-diff+(diff/numberOfPoints); d2<=d+diff; d2+=diff/numberOfPoints)
                {
                    double temp=Math.abs(function.calculate(d2));
                    if(temp < closestY)
                    {
                        closestY = temp;
                        closestX = d2;
                    }
                    else break;
                }
                if(closestY < 1e-5)
                {
                    rootFound=true;
                    Text txtRoot = new Text(f1.getFunction().getFunctionName() + " and " + f2.getFunction().getFunctionName() + " intersect at (" 
                                            + BigDecimal.valueOf(closestX).setScale(8, RoundingMode.HALF_UP).doubleValue() + ", " 
                                            + BigDecimal.valueOf(f1.getFunction().calculate(closestX)).setScale
                                            (8, RoundingMode.HALF_UP).doubleValue() + ")" );
                    txtRoot.setWrappingWidth(width);
                    vbxGraph.getChildren().add(1, txtRoot);
                }
            }
            yprev=ycur;
            ycur=ynext;
        } 

        if(!rootFound)
        {
            Text txtNoRoot = new Text(f1.getFunction().getFunctionName() + " and " + f2.getFunction().getFunctionName() + " do not intersect within their range");
            txtNoRoot.setWrappingWidth(width);
            vbxGraph.getChildren().add(1, txtNoRoot);
        }

        stgChooseFunction.hide();
        stgGraph.show();
    }

    //display the students of a given class and allow editing
    public void btnStudentsClicked(String className)
    {
        stgClasses.hide();
        Class cl = new Class();
        for(Class cur:classes)
        {
            if (cur.getName().equals(className))
            {
                cl=cur;
                break;
            }
        }
        VBox vbxStudents = new VBox();

        /*for each student have the program display their name and email
         *and button to delete
         */
        for(Student st:cl.getStudents())
        {
            HBox hc1 = new HBox();
            HBox hc2 = new HBox();
            HBox hc3 = new HBox();
            HBox hc4 = new HBox();//to leave space between the text and the buttons
            Text txtStudentName = new Text(st.getName());
            txtStudentName.setFont(new Font(16));
            hc1.getChildren().add(txtStudentName);
            hc1.setMinWidth(300);
            Text txtStudentEmail = new Text(st.getEmailAddress());
            txtStudentEmail.setFont(new Font(16));
            hc2.getChildren().add(txtStudentEmail);
            hc2.setMinWidth(300);
            Button btnDelete = new Button("Delete student");
            btnDelete.setOnAction(e->btnDeleteStudentClicked(className,st.getName(), st.getEmailAddress()));
            hc3.getChildren().addAll(hc1, hc2);
            hc4.getChildren().addAll(hc3, btnDelete);
            vbxStudents.getChildren().add(hc4);
        }

        ScrollPane spStudents = new ScrollPane();
        spStudents.setContent(vbxStudents);//in case there are too many students
        spStudents.setMaxHeight(500);

        Button btnAdd = new Button("Add student");
        btnAdd.setOnAction(e->btnAddStudentClicked(className));

        Button btnDone = new Button("Done");
        btnDone.setOnAction(e->{stgStudents.hide();btnClassesClicked();});

        HBox hbxBtns = new HBox();
        hbxBtns.getChildren().addAll(btnAdd, btnDone);
        hbxBtns.setAlignment(Pos.BASELINE_RIGHT);

        VBox vbxStudentsScene = new VBox();
        vbxStudentsScene.getChildren().addAll(spStudents, hbxBtns);

        stgStudents = new Stage();
        stgStudents.resizableProperty().setValue(false);
        /*if x button clicked or hotkey alt-f4 pressed
         * save changes and return to previous screen
         * just as if done button was clicked
         */
        stgStudents.setOnCloseRequest(e->{stgStudents.hide();btnClassesClicked();});
        Scene scnStudents = new Scene(vbxStudentsScene);
        stgStudents.setScene(scnStudents);
        stgStudents.setTitle("Students");
        stgStudents.show();
    }

    //delete student from a class
    public void btnDeleteStudentClicked(String clName, String name, String emailAddress)
    {
        Class cl=new Class();
        for(Class cur:classes)
        {
            if (cur.getName().equals(clName))
            {
                cl=cur;
                break;
            }
        }
        for(Student st:cl.getStudents())
        {
            if(st.getName().equals(name) && st.getEmailAddress().equals(emailAddress))
            {
                cl.getStudents().remove(st);
                break;
            }
        }
        stgStudents.hide();
        btnStudentsClicked(clName);
    }

    //add student to class
    public void btnAddStudentClicked(String clName)
    {
        stgStudents.hide();

        Class cl = new Class();
        for(Class cur:classes)
        {
            if(cur.getName().equals(clName))
            {
                cl=cur;
                break;
            }
        }

        TextField txtfStudentName = new TextField();
        txtfStudentName.setPromptText("Student Name");
        TextField txtfStudentEmail = new TextField();
        txtfStudentEmail.setPromptText("Student Email Address");

        Button btnAdd = new Button("Add Student");
        btnAdd.setOnAction(e->btnDoneAddingStudentClicked(clName, txtfStudentName.getText(), txtfStudentEmail.getText()));
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e->{stgAddStudent.hide();stgStudents.show();});

        HBox hbxSt = new HBox();
        hbxSt.getChildren().addAll(txtfStudentName, txtfStudentEmail, btnAdd, btnCancel);

        stgAddStudent = new Stage();
        stgAddStudent.resizableProperty().setValue(false);
        stgAddStudent.setOnCloseRequest(e->{stgAddStudent.hide();stgStudents.show();});
        Scene sc = new Scene(hbxSt);
        stgAddStudent.setScene(sc);
        stgAddStudent.setTitle("Add student");
        stgAddStudent.show();
    }

    //validate student name and email
    public void btnDoneAddingStudentClicked(String clName, String name, String email)
    {
        if (!(Validate.isName(name) && Validate.isEmail(email)))
        {
            throwError("Invalid name or email", stgAddStudent);
            return;
        }

        Class cl = new Class();
        for(Class cur:classes)
        {
            if(cur.getName().equals(clName))
            {
                cl=cur;
                break;
            }
        }

        for(Student st:cl.getStudents())
        {
            if(st.getEmailAddress().equals(email))
            {
                throwError("Email already exists", stgStudents);
                return;
            }
        }

        cl.getStudents().add(new Student(name, email));
        stgAddStudent.hide();
        btnStudentsClicked(clName);
    }

    //delete a class
    public void btnDeleteClassClicked(String className)
    {
        for(Class cur:classes)
        {
            stgClasses.hide();
            if (cur.getName().equals(className))
            {
                classes.remove(cur);
                btnClassesClicked();//build same window without the class that was deleted
                break;
            }
        }
    }

    //add a class
    public void btnAddClicked()
    {
        stgClasses.hide();

        TextField txtfName = new TextField("Name");
        Button btnAddClass = new Button("Add Class");
        btnAddClass.setOnAction(e->btnDoneAddingClicked(txtfName.getText()));
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e->{stgAddClass.hide();stgClasses.show();});
        HBox h = new HBox();
        h.getChildren().addAll(txtfName,btnAddClass, btnCancel);

        stgAddClass = new Stage();
        stgAddClass.resizableProperty().setValue(false);
        stgAddClass.setOnCloseRequest(e->{stgAddClass.hide();stgClasses.show();});
        Scene sc = new Scene(h);
        stgAddClass.setScene(sc);
        stgAddClass.setTitle("Add class");
        stgAddClass.show();
    }

    //validate uniqueness of class
    public void btnDoneAddingClicked(String name)
    {
        for (Class cl:classes)
        {
            if(cl.getName().equals(name))
            {
                throwError("Class already exists with this name", stgClasses);
                return;
            }
        }
        classes.add(new Class(name));
        stgAddClass.hide();
        btnClassesClicked();//build previous window again with the added class
    }

    /*Error window that displays error passed in as argument
     *prev is the stage that the program should return to after the error
     *has been read by the user
     */
    public void throwError(String err, Stage prev)
    {
        prev.hide();

        Text txtError = new Text(err);
        txtError.setFont(new Font(20));

        Button btnOK = new Button("OK");
        btnOK.setOnAction(e->{stgError.hide(); prev.show();});

        HBox h = new HBox();
        h.getChildren().addAll(txtError, btnOK);

        stgError = new Stage();
        stgError.resizableProperty().setValue(false);
        stgError.setOnCloseRequest(e->{stgError.hide(); prev.show();});
        Scene sc = new Scene(h);
        stgError.setScene(sc);
        stgError.setTitle("Error window");
        stgError.show();
    }

    //save all classes in a file
    public void saveClasses()
    {
        try
        {
            PrintWriter file = new PrintWriter(new FileWriter("Classes.txt"));
            file.println(classes.size());
            for(Class cl:classes)
            {
                file.println(cl.getName());
                file.println(cl.getStudents().size());
                for (Student st:cl.getStudents())
                {
                    file.println(st.getName());
                    file.println(st.getEmailAddress());
                }
            }
            file.close();
        }catch(Exception e){System.out.println("Error");return;}
    }
}