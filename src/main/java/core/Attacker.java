package core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attacker {

   protected Set<AttackStep> activeAttackSteps  = new HashSet<>();
   Boolean                   verbose            = false;
   public static String      defaultProfilePath = "./target/generated-sources/attackerProfile.ttc";
   protected static Map<String, Double> ttcHashMap       = new HashMap<>();

   public Attacker() {
      verbose = false;
   }

   public Attacker(boolean verbose) {
      this.verbose = verbose;
   }

   public void addAttackPoint(AttackStep attackPoint) {
      attackPoint.ttc = 0;
      activeAttackSteps.add(attackPoint);
   }

   public void addRandomAttackPoint(long randomSeed) {
      AttackStep attackPoint = AttackStep.randomAttackStep(randomSeed);
      System.out.println("Attack point: " + attackPoint.fullName());
      addAttackPoint(attackPoint);
   }

   private AttackStep getShortestActiveStep() {
      AttackStep shortestStep = null;
      double shortestTtc = Double.MAX_VALUE;
      for (AttackStep attackStep : activeAttackSteps) {
         if (attackStep.ttc < shortestTtc) {
            shortestTtc = attackStep.ttc;
            shortestStep = attackStep;
         }
      }
      return shortestStep;
   }

   public void reset() {
      for (AttackStep attackStep : AttackStep.allAttackSteps) {
         attackStep.ttc = Double.MAX_VALUE;
      }
   }

   private void debugPrint(String str) {
      if (verbose) {
         System.out.print(str + "\n");
      }
   }

   public void customizeTtc(String name, String distribution) {
      ttcHashMap.put(name, Attacker.parseDistribution(distribution));
   }

   public static double parseDistribution(String dist) {
      Pattern pattern = Pattern.compile("([a-z]+)\\(*([0-9.]+)*,*([0-9.]+)*\\)*", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(dist);
      matcher.matches();
      switch (matcher.group(1)) {
         case "Zero":
            return AttackStep.oneSecond;
         case "Infinity":
            return AttackStep.infinity;
         case "ExponentialDistribution":
            return Double.valueOf(matcher.group(2));
         case "GammaDistribution":
            return Double.valueOf(matcher.group(2)) * Double.valueOf(matcher.group(3));
         case "UniformDistribution":
            return (Double.valueOf(matcher.group(3)) - Double.valueOf(matcher.group(2))) / 2;
         default:
            System.err.printf("No matching distribution for: %s\n", dist);
            return AttackStep.oneSecond;
      }
   }

   private Map<String, Double> readProfile(Properties profile) {
      Map<String, Double> profileMap = new HashMap<>();
      for (String name : profile.stringPropertyNames()) {
         // Local ttc overrides ttcfile
         if(ttcHashMap.containsKey(name) ) {
            profileMap.put(name, ttcHashMap.get(name));
         } else {
            profileMap.put(name, parseDistribution(profile.getProperty(name)));
         }
      }
      ttcHashMap.clear();
      return profileMap;
   }

   public void attack() {
      System.err.println("No attacker profile selected! Trying default path...");
      attack(defaultProfilePath);
   }

   public void attack(String profilePath) {
      Properties profile = new Properties();
      try {
         profile.load(new FileInputStream(profilePath));
      }
      catch (IOException e) {
         System.err.printf("Could not open profile: %s\n", profilePath);
         System.exit(1);
      }
      attack(profile);
   }

   public void attack(Properties profile) {
      AttackStep.ttcHashMap = readProfile(profile);

      debugPrint("The model contains " + Integer.toString(Asset.allAssets.size()) + " assets and " + Integer.toString(AttackStep.allAttackSteps.size()) + " attack steps.");
      AttackStep currentAttackStep = null;
      debugPrint("AttackStep.allAttackSteps = " + AttackStep.allAttackSteps);

      for (AttackStep attackStep : AttackStep.allAttackSteps) {
         attackStep.setExpectedParents();
         debugPrint("The expected parents of " + attackStep.fullName() + " are " + attackStep.expectedParents);
      }

      for (Defense defense : Defense.allDefenses) {
         if (!defense.isEnabled()) {
            addAttackPoint(defense.disable);
         }
      }

      while (!activeAttackSteps.isEmpty()) {
         debugPrint("activeAttackSteps = " + activeAttackSteps);
         currentAttackStep = getShortestActiveStep();
         debugPrint("Updating children of " + currentAttackStep.fullName());
         currentAttackStep.updateChildren(activeAttackSteps);
         activeAttackSteps.remove(currentAttackStep);
      }
   }

}
