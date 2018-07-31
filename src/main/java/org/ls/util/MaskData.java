package org.ls.util;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.ajbrown.namemachine.NameGenerator;

import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

/**
 * Created by Leonardo Simberg on 7/30/2018.
 *
 * Used to Mask Account numbers and names
 */
public class MaskData {
    private static final int ACCOUNT_NUM_DIGITS = 8;

    private static final int POSITION_ACCOUNT = 2;
    private static final int POSITION_NAME = 1;
    private static final String FILE_SUFFIX = "mask";

    public static void main(String args[]) {
        System.out.println("Starting...");
        long beginTime = System.currentTimeMillis();
        NameGenerator generator = new NameGenerator(); //Generate Fake names


        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("     MaskData <input filename>");
            return;
        }

        String filenameIn = args[0];
        Path inputPath = Paths.get(filenameIn);
        String filenameOut = addFileSuffix(inputPath);
        System.out.println(filenameOut);

        try (
                Reader reader = Files.newBufferedReader(inputPath);
                CSVReader csvReader = new CSVReader(reader);
                CSVWriter writer = new CSVWriter(new FileWriter(filenameOut), ',');
        ){
            System.out.printf("Reading data file: \"%s\" ...\n", filenameIn);
            List<String[]> records = csvReader.readAll();

            System.out.println("Generating fake account numbers...");
            LongStream fakeAccounts = getUniqueAccounts(ACCOUNT_NUM_DIGITS, records.size()); //Generate Fake accounts
            PrimitiveIterator.OfLong fakeAccountsIterator = fakeAccounts.iterator();

            System.out.println("Masking file...");
            for(String[] record: records) {
                record[POSITION_ACCOUNT] = accFormat(fakeAccountsIterator.nextLong());
                record[POSITION_NAME] = generator.generateName().toString();
            }

            System.out.printf("Writing masked file \"%s\" ...\n", filenameOut);
            writer.writeAll(records, false);

            System.out.printf("Done - Processing time: %f s\n",((float)(System.currentTimeMillis()-beginTime))/1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Generate a fake and unique account number
     * @param numDig num of digits
     * @param limit maximum number of generated elements
     * @return A stream with the account numbers
     */
    private static LongStream getUniqueAccounts(int numDig, int limit) {
        long start = (long)Math.pow(10,numDig-1);
        long end = (long)Math.pow(10,numDig);
        return ThreadLocalRandom.current().longs(start, end).distinct().limit(limit);
    }

    private static String addFileSuffix(Path path) {
        String filename = path.getFileName().toString();
        int lastPointPosition = filename.lastIndexOf('.');
        if (lastPointPosition == 0) {
            return path.getParent() + "\\" + filename + "_" + FILE_SUFFIX;
        }
        String name = filename.substring(0, lastPointPosition);
        String termination = filename.substring(lastPointPosition+1, filename.length());
        return path.getParent() + "\\" + name + "_" + FILE_SUFFIX + "." + termination;
    }

    private static String accFormat(long account) {
        String accountS = Long.toString(account);
        return accountS.substring(0,3) + " " +  accountS.substring(3, accountS.length());
    }
}
