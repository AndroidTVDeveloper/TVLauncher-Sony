package android.support.p001v4.content.p002pm;

import java.util.ArrayList;
import java.util.List;

/* renamed from: android.support.v4.content.pm.ShortcutInfoCompatSaver */
public abstract class ShortcutInfoCompatSaver<T> {
    public abstract T addShortcuts(List<ShortcutInfoCompat> list);

    public abstract T removeAllShortcuts();

    public abstract T removeShortcuts(List<String> list);

    public List<ShortcutInfoCompat> getShortcuts() throws Exception {
        return new ArrayList();
    }

    /* renamed from: android.support.v4.content.pm.ShortcutInfoCompatSaver$NoopImpl */
    public static class NoopImpl extends ShortcutInfoCompatSaver<Void> {
        public Void addShortcuts(List<ShortcutInfoCompat> list) {
            return null;
        }

        public Void removeShortcuts(List<String> list) {
            return null;
        }

        public Void removeAllShortcuts() {
            return null;
        }
    }
}
