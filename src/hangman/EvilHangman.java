package hangman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class EvilHangman {

    public static void main (String[] args) throws IOException, EmptyDictionaryException, GuessAlreadyMadeException {
        String dictionaryFileName = args[0];
        int wordLength = Integer.parseInt(args[1]);
        int maxGuesses = Integer.parseInt(args[2]);
        int numGuesses = 0;
        File file = new File(dictionaryFileName);
        EvilHangmanGame game = new EvilHangmanGame();
        String key = getInitialWordRepresentation(wordLength);
        Set<String> currentWords = new HashSet<>();
        int numOfOccurrences = 0;
        boolean playerWon = false;

        game.startGame(file, wordLength);
        Scanner inputScanner = new Scanner(System.in);

        while (numGuesses < maxGuesses) {
            numOfOccurrences = 0;
            System.out.println("You have " + (maxGuesses - numGuesses) + " guesses left");
            System.out.println("Used letters:" + lettersToString(game.getGuessedLetters()));
            System.out.println("Word: " + key);
            System.out.println("Enter Guess: ");
            //Get a valid input
            boolean validInput = false;
            String guess = "";
            while (!validInput) {
                guess = inputScanner.nextLine();
                if (guess.equals("")) {
                    System.out.println("Invalid input! Enter guess: ");
                } else if (!Character.isLetter(guess.charAt(0)) || guess.length() != 1) {
                    System.out.println("Invalid input! Enter guess: ");
                } else {
                    try {
                        currentWords = game.makeGuess(guess.charAt(0));
                        validInput = true;
                        key = GenerateWordRepresentation(key, game.getSubsetKey());
                        ++numGuesses;
                        numOfOccurrences = getNumOccurrences(currentWords, guess.charAt(0));
                    } catch (GuessAlreadyMadeException e) {
                        System.out.println("Guess already made! Enter guess: ");
                    }
                }
            }
            //Display response from input
            if (numOfOccurrences == 0) {
                System.out.println("Sorry, there are no " + guess + "'s\n");
            } else if (numOfOccurrences == 1){
                System.out.println("Yes, there is " + numOfOccurrences + " " + guess + "\n");
            } else {
                System.out.println("Yes, there are " + numOfOccurrences + " " + guess + "'s\n");
            }
            if (isAlpha(key)) {
                System.out.println("You won!");
                System.out.println("The word was: " + GenerateWordRepresentation(key, game.getSubsetKey()));
                playerWon = true;
                break;
            }
        }
        if (!playerWon) {
            String winningWord = "";
            for (String word : currentWords) {
                winningWord = word;
                break;
            }
            System.out.println("You Lose!");
            System.out.println("The word was: " + winningWord);
        }
    }

    public static String lettersToString(SortedSet<Character> letters) {
        StringBuilder usedLetters = new StringBuilder();
        for (Character currentChar : letters) {
            usedLetters.append(' ');
            usedLetters.append(currentChar);
        }
        return usedLetters.toString();
    }

    public static String getInitialWordRepresentation(int numCharacters) {
        StringBuilder initialWord = new StringBuilder();
        for (int i = 0; i < numCharacters; ++i) {
            initialWord.append("-");
        }
        return initialWord.toString();
    }

    public static int getNumOccurrences(Set<String> currentWords, char guess) {
        int num = 0;
        int minNum = 0;
        for (String currentWord : currentWords) {
            num = 0;
            for (int i = 0; i < currentWord.length(); ++i) {
                if (currentWord.charAt(i) == guess) {
                    ++num;
                }
            }
            if (num > minNum) {
                minNum = num;
            }
        }
        return minNum;
    }

    public static String GenerateWordRepresentation(String currentWordRepresentation, String newWordRepresentation) {
        StringBuilder finalRepresentation = new StringBuilder(currentWordRepresentation);
        StringBuilder otherRepresentation = new StringBuilder(newWordRepresentation);
        int index = 0;
        for (char currentLetter : otherRepresentation.toString().toCharArray()) {
            if (Character.isLetter(currentLetter)) {
                finalRepresentation.setCharAt(index, currentLetter);
            }
            ++index;
        }
        return finalRepresentation.toString();
    }

    public static boolean isAlpha (String word) {
        for (char currentLetter : word.toCharArray()) {
            if (!Character.isLetter(currentLetter)) {
                return false;
            }
        }
        return true;
    }
}
