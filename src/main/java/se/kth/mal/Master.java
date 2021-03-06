package se.kth.mal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.foreseeti.generator.SecuriCADCodeGenerator;

public class Master {

   public Master(String malFilePath, String testsOutFolderPath, String javaOutFolderPath, String packageName, boolean useForeseeti, String visualFolderPath) {
      try {
         if (useForeseeti) {
            System.out.println(">>> Using foreseeti backend <<<");
            SecuriCADCodeGenerator generator = new SecuriCADCodeGenerator(malFilePath, testsOutFolderPath, javaOutFolderPath, packageName);
            generator.generate();
         }
         else {
            System.out.println(">>> Using MAL Compiler <<<");
            File malFile = new File(malFilePath);
            String inFolderPath = malFile.getParentFile().getAbsolutePath();
            String inFileName = malFile.getName();
            new CompilerWriter(inFolderPath, inFileName, inFolderPath, javaOutFolderPath, packageName, javaOutFolderPath);
         }
      }
      catch (IllegalArgumentException | IOException e) {
         e.printStackTrace();
      }
      System.out.println("Complete");
   }

   public static void main(String[] args) throws Exception {
      Options options = new Options();

      Option input = new Option("i", "input", true, "input mal file path");
      input.setRequired(true);
      options.addOption(input);

      Option output = new Option("o", "output", true, "output folder path for generated code");
      output.setRequired(true);
      options.addOption(output);

      Option tests = new Option("t", "tests", true, "output folder path for generated test code");
      tests.setRequired(false);
      options.addOption(tests);

      Option packageName = new Option("p", "package", true, "package name of generated code");
      packageName.setRequired(true);
      options.addOption(packageName);

      Option visual = new Option("v", "visual", true, "icons for visualization");
      visual.setRequired(false);
      options.addOption(visual);

      Option foreseeti = new Option("f", "foreseeti", false, "flag to use foreseeti backend");
      foreseeti.setRequired(false);
      options.addOption(foreseeti);

      CommandLineParser parser = new DefaultParser();
      HelpFormatter formatter = new HelpFormatter();
      CommandLine cmd = null;

      try {
         cmd = parser.parse(options, args);
         new Master(cmd.getOptionValue("input").trim(), cmd.getOptionValue("tests"), cmd.getOptionValue("output").trim(), cmd.getOptionValue("package").trim(), cmd.hasOption("foreseeti"),
               cmd.getOptionValue("visual"));
      }
      catch (ParseException e) {
         System.err.println(e.getMessage());
         formatter.printHelp("utility-name", options);
      }
   }
}
