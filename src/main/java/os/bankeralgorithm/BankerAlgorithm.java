
package os.bankeralgorithm;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class BankerAlgorithm {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        boolean continueProgram = true;

        while (continueProgram) {  
            
            System.out.println("Banker's Algorithm for Multiple Resource Allocation");
            System.out.println("===============================================");

            System.out.print("Enter Resource size: ");
            int resourceSize = scanner.nextInt();
            
            System.out.print("Enter Process size: ");
            int processSize = scanner.nextInt();

            int[][] maxMatrix = new int[processSize][resourceSize];
            int[][] allocationMatrix = new int[processSize][resourceSize];
            int[][] neededMatrix = new int[processSize][resourceSize];
            int[] availableVector = new int[resourceSize];
            
            System.out.print("Enter i to get input data from a file or press r for random data: ");
            scanner.nextLine(); 
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("I")) {
                System.out.print("Enter the file name (with .txt extension): ");
                String fileName = scanner.nextLine();
                try {
                    File file = new File(fileName);
                    Scanner fileScanner = new Scanner(file);
                    
                    for (int i = 0; i < processSize; i++) {
                        for (int j = 0; j < resourceSize; j++) {
                            maxMatrix[i][j] = fileScanner.nextInt();
                        }
                    }
                    
                    for (int i = 0; i < processSize; i++) {
                        for (int j = 0; j < resourceSize; j++) {
                            allocationMatrix[i][j] = fileScanner.nextInt();
                        }
                    }
                    
                    int[] totalVector = new int[resourceSize];
                    
                    for (int i = 0; i < resourceSize; i++) {
                        totalVector[i] = fileScanner.nextInt();
                    }

                    for (int j = 0; j < resourceSize; j++) {
                        int sumAlloc = 0;
                        for (int i = 0; i < processSize; i++) {
                            sumAlloc += allocationMatrix[i][j];
                        }
                        availableVector[j] = totalVector[j] - sumAlloc;
                    }

                    System.out.println("\nTotal resources (from file):");
                    printVector(totalVector, resourceSize);

                    
                    fileScanner.close();
                    
                } catch (FileNotFoundException e) {
                    System.out.println("File not found. Exiting program.");
                    return;
                }
            } else {
                for (int i = 0; i < resourceSize; i++) {
                    availableVector[i] = random.nextInt(10) + 5;
                }
                
                for (int i = 0; i < processSize; i++) {
                    for (int j = 0; j < resourceSize; j++) {
                        allocationMatrix[i][j] = random.nextInt(2);
                    }
                }
                
                for (int i = 0; i < processSize; i++) {
                    for (int j = 0; j < resourceSize; j++) {
                        maxMatrix[i][j] = allocationMatrix[i][j] + random.nextInt(5) + 1;
                    }
                }
                
                System.out.println("Generated data successfully.");
            }

            calculateNeededMatrix(maxMatrix, allocationMatrix, neededMatrix, processSize, resourceSize);

            System.out.println("\nGiven Dataset:");
            System.out.println("==============");
            System.out.println("Max Matrix:");
            printMatrix(maxMatrix, processSize, resourceSize);
            
            System.out.println("\nAllocation Matrix:");
            printMatrix(allocationMatrix, processSize, resourceSize);
            
            System.out.println("\nNeeded Matrix:");
            printMatrix(neededMatrix, processSize, resourceSize);
            
            System.out.println("\nAvailable vector:");
            printVector(availableVector, resourceSize);

            System.out.println("\nStarting iterations to find safe sequence...");
            System.out.println("#############################################");
            
            boolean[] finished = new boolean[processSize];
            int[] safeSequence = new int[processSize];
            int count = 0;
            int iteration = 0;
            boolean deadlockDetected = true;

            while (count < processSize && iteration < processSize) {
                System.out.println("\nIteration " + iteration);
                deadlockDetected = true;

                for (int i = 0; i < processSize; i++) {
                    if (!finished[i] && canAllocate(i, neededMatrix, availableVector, resourceSize)) {
                        deadlockDetected = false;
                        
                        for (int j = 0; j < resourceSize; j++) {
                            availableVector[j] += allocationMatrix[i][j];
                            allocationMatrix[i][j] = 0;
                            neededMatrix[i][j] = 0;
                        }
                        safeSequence[count] = i;
                        finished[i] = true;
                        count++;

                        System.out.println("\nAfter allocating resources to P" + i + ":");
                        System.out.println("\nAllocation Matrix:");
                        printMatrix(allocationMatrix, processSize, resourceSize);
                        System.out.println("\nNeeded Matrix:");
                        printMatrix(neededMatrix, processSize, resourceSize);
                        System.out.println("\nAvailable vector:");
                        printVector(availableVector, resourceSize);
                        break;
                    }
                }

                if (deadlockDetected) {
                    System.out.println("\nDEADLOCK DETECTED: System is in unsafe state!");
                    System.out.println("No safe sequence exists.");
                    System.out.println("Deadlocked processes: ");
                    for (int i = 0; i < processSize; i++) {
                        if (!finished[i]) {
                            System.out.print("P" + i + " ");
                        }
                    }
                    System.out.println();
                    break;
                }
                iteration++;
            }

            if (count == processSize) {
                System.out.println("\nThe resource allocation has been completed within " + iteration + " iterations!\n");
                System.out.print("Safe sequence is => ");
                for (int i = 0; i < count; i++) {
                    System.out.print("P" + safeSequence[i]);
                    if (i < count - 1) System.out.print(" -> ");
                }
                System.out.println();
            }

            System.out.print("\nPress y to continue or any key to exit: ");
            String continueChoice = scanner.nextLine();
            continueProgram = continueChoice.equalsIgnoreCase("y");
            
            if (continueProgram) {
                System.out.println("\n"); 
            } else {
                System.out.println("Exiting program...");
            }
        }
        
        scanner.close();
    }

    private static void calculateNeededMatrix(int[][] max, int[][] allocation, int[][] needed, 
                                            int processSize, int resourceSize) {
        for (int i = 0; i < processSize; i++) {
            for (int j = 0; j < resourceSize; j++) {
                needed[i][j] = max[i][j] - allocation[i][j];
            }
        }
    }

    private static boolean canAllocate(int process, int[][] needed, int[] available, int resourceSize) {

        boolean needsResources = false;
        for (int i = 0; i < resourceSize; i++) {
            if (needed[process][i] > 0) {
                needsResources = true;
                break;
            }
        }
        if (!needsResources) {
            return false;  
        }

        for (int i = 0; i < resourceSize; i++) {
            if (needed[process][i] > available[i]) {
                return false;
            }
        }
        return true;
    }

    private static void printMatrix(int[][] matrix, int processSize, int resourceSize) {
        System.out.print("    ");
        for (int i = 0; i < resourceSize; i++) {
            System.out.printf("R%d  ", i);
        }
        System.out.println();

        for (int i = 0; i < processSize; i++) {
            System.out.printf("P%d  ", i);
            for (int j = 0; j < resourceSize; j++) {
                System.out.printf("%-3d ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    private static void printVector(int[] vector, int size) {
        for (int i = 0; i < size; i++) {
            System.out.print("R" + i + " = " + vector[i]);
            if (i < size - 1) System.out.print(", ");
        }
        System.out.println();
    }
}