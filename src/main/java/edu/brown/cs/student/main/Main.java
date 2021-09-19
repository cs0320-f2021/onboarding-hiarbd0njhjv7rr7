package edu.brown.cs.student.main;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.util.*;

//import java

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;
  // variable to store a StarList which is a class which is used to load stars
  // from file and query them
  private StarList stars;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    // TODO: Add your REPL here!
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;
      while ((input = br.readLine()) != null) {
        try {
          input = input.trim();
          String[] arguments = input.split(" ");
          // System.out.println(arguments[0]);
          // TODO: complete your REPL by adding commands for addition "add" and subtraction
          //  "subtract"
          switch (arguments[0]) {
            case "add":
              if (arguments.length != 3){
                System.out.println("ERROR:");
              } else {
                String add_res = processAdd(arguments[1], arguments[2]);
                System.out.println(add_res);
              }
              break;
            case "subtract":
              if (arguments.length != 3){
                System.out.println("Invalid number of inputs.");
              } else {
                String sub_res = processSubtract(arguments[1], arguments[2]);
                System.out.println(sub_res);
              }
              break;
            case "stars":
              processStars(arguments);
              break;
            case "naive_neighbors":
              processNaiveNeighbours(arguments);
              break;
            default: 
              System.out.println("ERROR: We couldn't process your input");
          }
        } catch (Exception e) {
           e.printStackTrace();
          System.out.println("ERROR: We couldn't process your input");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: Invalid input for REPL");
    }

  }

  private void processStars(String[] arguments){
    if (arguments.length != 2){
      System.out.println("Invalid number of inputs.");
    } else {
      try {
        this.stars = new StarList(arguments[1]);
      } catch (IllegalArgumentException e){
        System.out.println("Bad file.");
      }
    }
  }

  private void processNaiveNeighbours(String[] arguments){

    if (arguments.length < 3) {
      System.out.println("Invalid number of inputs.");
    } else {
      Star[] res = null;
      if (arguments[2].startsWith("\"")) {
        try {
          int k;
          String starName = String.join(" ", Arrays.copyOfRange(arguments, 2, arguments.length));
          starName = starName.replace("\"", "");
          k = Integer.parseInt(arguments[1]);
          res = this.stars.findNeighboursByName(starName, k);

        } catch(Exception e) {
          e.printStackTrace();
          System.out.println("2 arg naive");
        }
      } else {
        if (arguments.length == 5) {
          try {
            int k = Integer.parseInt(arguments[1]);
            Double x = Double.parseDouble(arguments[2]);
            Double y = Double.parseDouble(arguments[3]);
            Double z = Double.parseDouble(arguments[4]);
            res = this.stars.findNeighboursByCoordinates(k, x, y, z, this.stars.records);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
      if (res != null){
        System.out.println("Results:");
        boolean empty = true;
        for (Star star : res){
          if (star != null){
            empty = false;
            System.out.println(star.toString());
          }
        }
        if (empty){
          System.out.println("No stars found.");
        }
      }

    }

  }



  private static String processAdd(String str1, String str2) {
    Double dbl1;
    Double dbl2;
    MathBot mb = new MathBot();
    try {
      dbl1 = Double.parseDouble(str1);
      dbl2 = Double.parseDouble(str2);
      } catch (Exception e) {
        throw new IllegalArgumentException("weewoo");
        // e.printStackTrace();
      }
    Double res = mb.add(dbl1, dbl2);
    return Double.toString(res);
  }

  private static String processSubtract(String str1, String str2) {
    Double dbl1;
    Double dbl2;
    MathBot mb = new MathBot();
    try {
      dbl1 = Double.parseDouble(str1);
      dbl2 = Double.parseDouble(str2);
      } catch (Exception e) {
        throw new IllegalArgumentException("weewoo");
        // e.printStackTrace();
      }
    Double res = mb.subtract(dbl1, dbl2);
    return Double.toString(res);
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_0);

    // this is the directory where FreeMarker templates are placed
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    // set port to run the server on
    Spark.port(port);

    // specify location of static resources (HTML, CSS, JS, images, etc.)
    Spark.externalStaticFileLocation("src/main/resources/static");

    // when there's a server error, use ExceptionPrinter to display error on GUI
    Spark.exception(Exception.class, new ExceptionPrinter());

    // initialize FreeMarker template engine (converts .ftl templates to HTML)
    FreeMarkerEngine freeMarker = createEngine();

    // setup Spark Routes
    Spark.get("/", new MainHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      // status 500 generally means there was an internal server error
      res.status(500);

      // write stack trace to GUI
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to serve the site's main page.
   *
   * @return ModelAndView to render.
   * (main.ftl).
   */
  private static class MainHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      // this is a map of variables that are used in the FreeMarker template
      Map<String, Object> variables = ImmutableMap.of("title",
          "Go go GUI");

      return new ModelAndView(variables, "main.ftl");
    }
  }

  private class StarList {

    ArrayList<Star> records;
    HashMap<String, Star> nameMap;

    public StarList(String filename){
      this.records = new ArrayList<Star>();
      this.nameMap = new HashMap<String, Star>();
      loadStars(filename);
    }

    /**
     *
     * @param starName
     * @param k
     * @return
     */
    private Star[] findNeighboursByName(String starName, int k){
      Star findStar = this.nameMap.get(starName);

      if (findStar == null){
        throw new IllegalArgumentException("Star not found.");
      }
      Double x = findStar.getX();
      Double y = findStar.getY();
      Double z = findStar.getZ();
      ArrayList<Star> searchArray = new ArrayList<Star>(this.records);
      searchArray.remove(findStar);

      return findNeighboursByCoordinates(k, x, y, z, searchArray);
    }

    private Star[] findNeighboursByCoordinates(int k, Double x, Double y, Double z, ArrayList<Star> searchRecords){

      Double currMin = Double.MAX_VALUE;
      Star[] result = new Star[k];
      Double[] resultDistances = new Double[k];
      for (int idx = 0; idx < resultDistances.length; idx++){
        resultDistances[idx] = Double.MAX_VALUE;
      }

      for (Star otherStar : searchRecords){
        Double currDistance = getDistance(x, y, z, otherStar.getX(), otherStar.getY(), otherStar.getZ());



        if (currDistance < currMin){
          int insertIndex = -1;
          for (int i = 0; i < k; i++){
            if (currDistance < resultDistances[i]){
              insertIndex = i;
              break;
            }
          }

          for (int j = k - 1; j > insertIndex; j--){
            result[j] = result[j-1];
            resultDistances[j] = resultDistances[j-1];
          }

          resultDistances[insertIndex] = currDistance;
          result[insertIndex] = otherStar;
          currMin = resultDistances[resultDistances.length-1];
        }
      }
      return result;
    }

    private void loadStars(String filename){

      try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;

        // Check validity
        String header = br.readLine();
        String[] fileHeaders = header.split(",");
        String[] correctHeader = {
          "StarID",
          "ProperName",
          "X",
          "Y",
          "Z"
        };

        if (fileHeaders.length != 5){
          throw new IllegalArgumentException("Input file does not have 5 columns.");
        }
        for (int i = 0; i < fileHeaders.length; i++) {
          if (!fileHeaders[i].equals(correctHeader[i])){
            throw new IllegalArgumentException("Input file has " + fileHeaders[i] + " column but expected "+ correctHeader[i] + "instead.");
          }
        }

        while ((line = br.readLine()) != null) {
          String[] values = line.split(",");

          if (values.length != 5){
            System.out.println("Bad row.");
            continue;
          }
          Star currStar = new Star(values[0], values[1], values[2], values[3], values[4]);
          this.records.add(currStar);
          this.nameMap.put(currStar.getStarName(), currStar);
        }
        this.records = records;
      } catch (FileNotFoundException e){
        System.out.println("File Not Found.");
      } catch (IOException e){
        System.out.println("IO Exception.");
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    public Double getDistance(Double x1, Double y1, Double z1, Double x2, Double y2, Double z2){
      return Math.sqrt(Math.pow(x1 - x2, 2) +
        Math.pow(y1 - y2, 2) +
        Math.pow(z1 - z2, 2));
    }
  }

  private class Star {
    int starID;
    String starName;
    Double x_coord;
    Double y_coord;
    Double z_coord;

    public Star(String rawStarID, String rawStarName, String rawX, String rawY, String rawZ){
      try {
        this.starID = Integer.parseInt(rawStarID);
        this.x_coord = Double.parseDouble(rawX);
        this.y_coord = Double.parseDouble(rawY);
        this.z_coord = Double.parseDouble(rawZ);
        if (rawStarName.length() == 0){
          this.starName = "<no name>";
        } else {
          this.starName = rawStarName;
        }
        System.out.println(this.starName);
      } catch (NumberFormatException e){
        System.out.println("Number format exception.");
      }
    }

    public String getStarName(){
      return this.starName;
    }

    public Double getX(){
      return this.x_coord;
    }

    public Double getY(){
      return this.y_coord;
    }

    public Double getZ(){
      return this.z_coord;
    }

    @Override
    public String toString() {
      return String.format("Star ID: " + this.starID +
        " Star Name: " + this.starName +
        " X: " + this.x_coord +
        " Y: " + this.y_coord +
        " Z: " + this.z_coord);
    }
  }
}
