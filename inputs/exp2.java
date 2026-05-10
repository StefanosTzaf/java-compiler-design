class MyMain {
    public static void main(String[] args) {
        int temp;
        System.out.println(1);
    }
}

class A {
    int x;
    boolean y;
    public int foo(int n) {
        return n;
    }
    // Overloading
    public int foo(boolean b) { 
        return 1; 
    }
}

class B extends A {
    int z;
    // overriding
    public int foo(int n) { 
        return n;
    }
    // overloading
    public int foo(int n, int m) {
        return n;
    }
    public int bar() {
        return 0;
    }
}