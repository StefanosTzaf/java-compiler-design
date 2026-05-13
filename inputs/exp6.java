class MyMain {
    public static void main(String[] args) {
        int[] arr;
        boolean b;
        // this should pass
        arr = new int[5];
        b = true;
        // this should fail
        b = b[3];
        System.out.println(arr.length);
    }
}
