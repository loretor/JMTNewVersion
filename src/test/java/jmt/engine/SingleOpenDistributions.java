package jmt.engine;

import static org.junit.Assert.*;

import java.util.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import jmt.commandline.Jmt;

public class SingleOpenDistributions {

    @Test
    public void burstgeneral01() throws Exception {
        File input = File.createTempFile("burst-general-01", ".xml");
        File result = File.createTempFile("burst-general-01-result", ".xml");
        File expected = new File(getClass().getResource("burst-general-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("burst-general-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void burstmap01() throws Exception {
        File input = File.createTempFile("burst-map-01", ".xml");
        File result = File.createTempFile("burst-map-01-result", ".xml");
        File expected = new File(getClass().getResource("burst-map-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("burst-map-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void burstmmpp01() throws Exception {
        File input = File.createTempFile("burst-mmpp-01", ".xml");
        File result = File.createTempFile("burst-mmpp-01-result", ".xml");
        File expected = new File(getClass().getResource("burst-mmpp-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("burst-mmpp-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void coxian01() throws Exception {
        File input = File.createTempFile("coxian-01", ".xml");
        File result = File.createTempFile("coxian-01-result", ".xml");
        File expected = new File(getClass().getResource("coxian-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("coxian-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void deterministic01() throws Exception {
        File input = File.createTempFile("deterministic-01", ".xml");
        File result = File.createTempFile("deterministic-01-result", ".xml");
        File expected = new File(getClass().getResource("deterministic-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("deterministic-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void erlang01() throws Exception {
        File input = File.createTempFile("erlang-01", ".xml");
        File result = File.createTempFile("erlang-01-result", ".xml");
        File expected = new File(getClass().getResource("erlang-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("erlang-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void exponential01() throws Exception {
        File input = File.createTempFile("exponential-01", ".xml");
        File result = File.createTempFile("exponential-01-result", ".xml");
        File expected = new File(getClass().getResource("exponential-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("exponential-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void gamma01() throws Exception {
        File input = File.createTempFile("gamma-01", ".xml");
        File result = File.createTempFile("gamma-01-result", ".xml");
        File expected = new File(getClass().getResource("gamma-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("gamma-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void hyperexp01() throws Exception {
        File input = File.createTempFile("hyperexp-01", ".xml");
        File result = File.createTempFile("hyperexp-01-result", ".xml");
        File expected = new File(getClass().getResource("hyperexp-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("hyperexp-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void normal01() throws Exception {
        File input = File.createTempFile("normal-01", ".xml");
        File result = File.createTempFile("normal-01-result", ".xml");
        File expected = new File(getClass().getResource("normal-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("normal-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void pareto01() throws Exception {
        File input = File.createTempFile("pareto-01", ".xml");
        File result = File.createTempFile("pareto-01-result", ".xml");
        File expected = new File(getClass().getResource("pareto-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("pareto-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void phasetype01() throws Exception {
        File input = File.createTempFile("phasetype-01", ".xml");
        File result = File.createTempFile("phasetype-01-result", ".xml");
        File expected = new File(getClass().getResource("phasetype-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("phasetype-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

    @Test
    public void uniform01() throws Exception {
        File input = File.createTempFile("uniform-01", ".xml");
        File result = File.createTempFile("uniform-01-result", ".xml");
        File expected = new File(getClass().getResource("uniform-01-output.xml").toURI());
        FileUtils.copyFile(new File(getClass().getResource("uniform-01-input.xml").toURI()), input);

        String[] args = {"", "", "-maxtime", "60", "-seed", "1"};
        Map<String, String> options = Jmt.parseParameters(args, 2);
        assertTrue("Solver was not able to solve file!", Jmt.sim(input, result, options));

        /* This part of the code removes the first row from the result file */
        Scanner scanner = new Scanner(result);
        ArrayList<String> coll = new ArrayList<String>();
        scanner.nextLine();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }
        coll.remove(coll.size() - 1);
        scanner.close();

        FileWriter writer = new FileWriter(result);
        for (String line : coll) {
            writer.write(line);
        }
        writer.close();
        /* The row has now been removed */

        String strExp = FileUtils.readFileToString(expected, "utf-8");
        String strRes = FileUtils.readFileToString(result, "utf-8");
        strRes = strRes.replaceFirst("\\s+", "");
        String strDiff = StringUtils.difference(strExp, strRes);

        assertTrue(": Real output differs from expected!\n" + "Expected: \n" + strExp + "\nActual: \n" + strRes + "\nDiff: \n" + strDiff, strDiff.length() == 0);
    }

}
