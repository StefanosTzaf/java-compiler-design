class ThisInMain {
    public static void main(String[] a){
        int x;
        x = this.foo();
    }
}

class A {
    public int foo(){
        return 1;
    }
}