package com.example.javase.jvm;

public class GC {

    public static GC SAVE_HOOK = null;

    public static void main(String[] args) throws InterruptedException {
        // 新建对象，因为SAVE_HOOK指向这个对象，对象此时的状态是(reachable,unfinalized)
        SAVE_HOOK = new GC();
        //将SAVE_HOOK设置成null，此时刚才创建的对象就不可达了，因为没有句柄再指向它了，对象此时状态是(unreachable，unfinalized)
        SAVE_HOOK = null;
        //强制系统执行垃圾回收，系统发现刚才创建的对象处于unreachable状态，并检测到这个对象的类覆盖了finalize方法，因此把这个对象放入F-Queue队列，由低优先级线程执行它的finalize方法，此时对象的状态变成(unreachable, finalizable)或者是(finalizer-reachable,finalizable)
        System.gc();
        // sleep，目的是给低优先级线程从F-Queue队列取出对象并执行其finalize方法提供机会。在执行完对象的finalize方法中的super.finalize()时，对象的状态变成(unreachable,finalized)状态，但接下来在finalize方法中又执行了SAVE_HOOK = this;这句话，又有句柄指向这个对象了，对象又可达了。因此对象的状态又变成了(reachable, finalized)状态。
        Thread.sleep(500);
        // 这里楼主说对象处于(reachable,finalized)状态应该是合理的。对象的finalized方法被执行了，因此是finalized状态。又因为在finalize方法是执行了SAVE_HOOK=this这句话，本来是unreachable的对象，又变成reachable了。
        if (null != SAVE_HOOK) { //此时对象应该处于(reachable, finalized)状态
            // 这句话会输出，注意对象由unreachable，经过finalize复活了。
            System.out.println("Yes , I am still alive");
        } else {
            System.out.println("No , I am dead");
        }
        // 再一次将SAVE_HOOK放空，此时刚才复活的对象，状态变成(unreachable,finalized)
        SAVE_HOOK = null;
        // 再一次强制系统回收垃圾，此时系统发现对象不可达，虽然覆盖了finalize方法，但已经执行过了，因此直接回收。
        System.gc();
        // 为系统回收垃圾提供机会
        Thread.sleep(500);
        if (null != SAVE_HOOK) {
            // 这句话不会输出，因为对象已经彻底消失了。
            System.out.println("Yes , I am still alive");
        } else {
            System.out.println("No , I am dead");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("execute method finalize()");
        // 这句话让对象的状态由unreachable变成reachable，就是对象复活
        SAVE_HOOK = this;
    }
}