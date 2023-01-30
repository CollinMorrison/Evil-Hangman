package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.Character.toLowerCase;

public class EvilHangmanGame implements IEvilHangmanGame{
    private Set<String> wordSet;
    private String subsetKey;
    private SortedSet<Character> guessedCharacters;
    private char currentGuess;

    public EvilHangmanGame (){
        this.wordSet = new HashSet<>();
        this.subsetKey = "";
        this.guessedCharacters = new TreeSet<>();
    }

    /**
     * Loads the dictionary from the file into the word set
     * @param dictionary Dictionary of words to use for the game
     * @param wordLength Number of characters in the word to guess
     * @throws IOException
     * @throws EmptyDictionaryException
     */
    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        if (!this.wordSet.isEmpty()) {
            this.wordSet.clear();
        }
        Scanner scanner = new Scanner(dictionary);
        if (!scanner.hasNext()) {
            throw new hangman.EmptyDictionaryException();
        }
        while (scanner.hasNext()) {
            String str = scanner.next();
            if (str.length() == wordLength) {
                this.wordSet.add(str);
            }
        }
        if (this.wordSet.isEmpty()) {
            throw new hangman.EmptyDictionaryException();
        }
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        guess = toLowerCase(guess);
        if (this.guessedCharacters.contains(guess)) {
            throw new GuessAlreadyMadeException();
        }
        this.currentGuess = guess;
        this.guessedCharacters.add(guess);
        partitionWordSet(guess);
        return this.wordSet;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return this.guessedCharacters;
    }

    public String getSubsetKey(String word, char guessedLetter) {
        StringBuilder subsetKey = new StringBuilder(word);
        for (int i = 0; i < subsetKey.length(); ++i) {
            if (subsetKey.charAt(i) != guessedLetter) {
                subsetKey.setCharAt(i, '-');
            }
        }
        return subsetKey.toString();
    }

    public void partitionWordSet(char guessedCharacter) {
        HashMap<String, Set<String>> wordMap = new HashMap<>();
        for (String word : this.wordSet) {
            String subsetKey = getSubsetKey(word, guessedCharacter);
            //add the word to the applicable subset
            wordMap.computeIfAbsent(subsetKey, k -> new HashSet<String>());
            wordMap.get(subsetKey).add(word);
        }
        this.wordSet = getLargestSubset(wordMap);
    }

    private Set<String> getLargestSubset(HashMap<String, Set<String>> wordMap) {
        Map<String, Set<String>> largestSetMap = new HashMap<>();
        Set<String> largestSet = new HashSet<>();
        int largestSetSize = 0;
        //find the largest size
        for (Map.Entry<String, Set<String>> entry : wordMap.entrySet()) {
            if (entry.getValue().size() > largestSetSize) {
                largestSetSize = entry.getValue().size();
                largestSet = entry.getValue();
                this.subsetKey = entry.getKey();
            }
        }
        //add all the sets with that size
        for (Map.Entry<String, Set<String>> entry : wordMap.entrySet()) {
            if (entry.getValue().size() == largestSetSize) {
                largestSetMap.put(entry.getKey(), entry.getValue());
            }
        }
        if (largestSetMap.size() > 1) {
            //return the set that doesn't contain the letter at all
            for (Map.Entry<String, Set<String>> entry : largestSetMap.entrySet()) {
                if (!entry.getKey().contains(String.valueOf(this.currentGuess))) {
                    return entry.getValue();
                }
            }
            //return the set with the fewest letters
            int minSet = this.subsetKey.length();
            int currentFrequency;
            Map<String, Set<String>> largestSetMapFewestCharacters = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : largestSetMap.entrySet()) {
                currentFrequency = 0;
                //find how many times the guess shows up in the subset key
                for (char currentChar : entry.getKey().toCharArray()) {
                    if (currentChar == this.currentGuess) {
                        ++currentFrequency;
                    }
                }
                //set the minimum one
                if (currentFrequency < minSet) {
                    minSet = currentFrequency;
                }
            }
            //add all sets with that minimum size
            for (Map.Entry<String, Set<String>> entry : largestSetMap.entrySet()) {
                currentFrequency = 0;
                //find how many times the guess shows up in the subset key
                for (char currentChar : entry.getKey().toCharArray()) {
                    if (currentChar == this.currentGuess) {
                        ++currentFrequency;
                    }
                }
                //add the ones with the minimum value to the map
                if (currentFrequency == minSet) {
                    largestSetMapFewestCharacters.put(entry.getKey(), entry.getValue());
                }
            }
            //if the map has one member, return that set
            if (largestSetMapFewestCharacters.size() == 1) {
                for (Map.Entry<String, Set<String>> entry : largestSetMapFewestCharacters.entrySet()) {
                    return entry.getValue();
                }
            }
            //otherwise choose the set with the rightmost character
            Map<String, Set<String>> largestSetMapFewestCharactersFarthestRight = GetLargestSetMapFewestCharactersFarthestRight(largestSetMapFewestCharacters, 1);
            //if there's only one set left, return that set
            int numCharacters = 1;
            while (largestSetMapFewestCharactersFarthestRight.size() > 1) {
                largestSetMapFewestCharactersFarthestRight = GetLargestSetMapFewestCharactersFarthestRight(largestSetMapFewestCharactersFarthestRight, numCharacters);
                ++numCharacters;
            }
            for (Map.Entry<String, Set<String>> entry : largestSetMapFewestCharactersFarthestRight.entrySet()) {
                return entry.getValue();
            }
        }
        return largestSet;
    }

    public String getSubsetKey() {
        return this.subsetKey;
    };

    private Map<String, Set<String>> GetLargestSetMapFewestCharactersFarthestRight(Map<String, Set<String>> largestSetMapFewestCharacters, int numCharacters) {
        int index = 0;
        int tempIndex;
        int sameCharacterCounter = 0;
        for (Map.Entry<String, Set<String>> entry : largestSetMapFewestCharacters.entrySet()) {
            tempIndex = 0;
            sameCharacterCounter = 0;
            for (char currentChar : entry.getKey().toCharArray()) {
                if (currentChar == currentGuess) {
                    ++sameCharacterCounter;
                }
                if (sameCharacterCounter == numCharacters) {
                    break;
                }
                ++tempIndex;
            }
            if (tempIndex > index) {
                index = tempIndex;
            }
        }
        Map<String, Set<String>> largestSetMapFewestCharactersFarthestRight = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : largestSetMapFewestCharacters.entrySet()) {
            tempIndex = 0;
            sameCharacterCounter = 0;
            for (char currentChar : entry.getKey().toCharArray()) {
                if (currentChar == currentGuess) {
                    ++sameCharacterCounter;
                }
                if (sameCharacterCounter == numCharacters) {
                    break;
                }
                ++tempIndex;
            }
            if (tempIndex == index) {
                index = tempIndex;
                largestSetMapFewestCharactersFarthestRight.put(entry.getKey(), entry.getValue());
            }
        }
        return largestSetMapFewestCharactersFarthestRight;
    }
}
