package evaluator;

import java.util.*;

public class IgnoreList {
    private static IgnoreList instance;
    private Map<String, Set<String>> ignoreMap;

    private IgnoreList() {
        ignoreMap = new HashMap<>();
    }

    public static IgnoreList getInstance() {
        if (instance == null) {
            instance = new IgnoreList();
        }
        return instance;
    }

    public void addToIgnoreList(String action, String... strings) {
        Set<String> set = ignoreMap.computeIfAbsent(action, k -> new HashSet<>());
        set.addAll(Arrays.asList(strings));
    }

    public Set<String> getIgnoreList(String action) {
        return ignoreMap.getOrDefault(action, Collections.emptySet());
    }

    public boolean shouldIgnore(String target_action, String target_param) {
        return getIgnoreList(target_action).contains(target_param);
    }

}
