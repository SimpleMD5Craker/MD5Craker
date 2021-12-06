package frame.common;

public class Utils {
    public static class Pair<V0, V1> {
        private V0 v0;

        private V1 v1;

        public Pair(V0 v0, V1 v1) {
            this.v0 = v0;
            this.v1 = v1;
        }

        public V0 getV0() {return v0;}

        public V1 getV1() {return v1;}

        public void setV1(V1 newVal) {
            v1 = newVal;
        }

        public void setV0(V0 newVal) {
            v0 = newVal;
        }

    }
}
