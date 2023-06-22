import java.util.*;

public class Main {
    static final int MAX_ROUTES = 1000;
    static final int ROUTE_LENGTH = 100;
    static final String LETTERS_GEN = "RLRFR";
    static final char COMMAND = 'R';
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(MAX_ROUTES);

        Thread freqlog = new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!sizeToFreq.isEmpty()) {
                        int maxFreqKey = findMaxRegular();
                        System.out.println(String.format("\n\nСамое частое количество повторений %d (встретилось %d раз)\n\n", maxFreqKey, sizeToFreq.get(maxFreqKey)));
                    }
                }
            }
        });
        freqlog.start();

        for (int i = 0; i < MAX_ROUTES; i++) {
            Runnable task = () -> {
                String route = generateRoute(LETTERS_GEN, ROUTE_LENGTH);
                int frequency = getFrequency(route, COMMAND);
                System.out.println(String.format("%s (%s)--> %d times.", route, COMMAND, frequency));

                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(frequency)) {
                        sizeToFreq.replace(frequency, sizeToFreq.get(frequency) + 1);
                    } else {
                        sizeToFreq.put(frequency, 1);
                    }
                    sizeToFreq.notify();
                }
            };
            Thread th = new Thread(task);
            th.start();
            threads.add(th);
        }
        for (Thread th : threads) {
            th.join();
        }
        freqlog.interrupt();

        int maxFreqKey = findMaxRegular();
        System.out.println(String.format("Самое частое количество повторений %d (встретилось %d раз)", maxFreqKey, sizeToFreq.get(maxFreqKey)));
        System.out.println("Другие размеры:");
        for (Integer key : sizeToFreq.keySet()) {
            if (key != maxFreqKey) {
                System.out.println(String.format("- %d (%d раз)", key, sizeToFreq.get(key)));
            }
        }
    }

    public static int findMaxRegular() {
        int maxFreq = 0;
        int key = 0;
        for (Integer k : sizeToFreq.keySet()) {
            if (sizeToFreq.get(k) > maxFreq) {
                maxFreq = sizeToFreq.get(k);
                key = k;
            }
        }
        return key;
    }

    public static int getFrequency(String str, char letter) {
        int frequency = 0;
        for (char c : str.toCharArray()) {
            if (c == letter) {
                frequency++;
            }
        }
        return frequency;
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
