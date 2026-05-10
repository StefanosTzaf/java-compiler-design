class MyMain {
    public static void main(String[] args) {
        int temp;
        System.out.println(1);
    }
}

class A{
    int i;
    boolean flag;
    int j;
    public int foo() {
        return 0;
    }
    public boolean fa() {
        return false;
    }
}

class B extends A{
    A type;
    int k;
    public int foo() {
        return 0;
    }
    public boolean bla() {
        return false;
    }
}

class C extends B{
}

class D extends C{
    int m;
}

class E extends A{
    boolean flag;
}