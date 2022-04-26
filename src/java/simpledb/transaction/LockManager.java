package simpledb.transaction;

import simpledb.common.Permissions;
import simpledb.storage.PageId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * 锁管理器,基于PageID
 */
public class LockManager {
    private HashMap<PageId, Data> dataLockMap = new HashMap<PageId, Data>();

    private Data getData(PageId pid) {
        if (dataLockMap.containsKey(pid))
        {
            return dataLockMap.get(pid);
        }
        var data = new Data(pid);
        dataLockMap.put(pid, data);
        return data;
    }

    private List<LockStatus> getLockStatus(PageId pid) {
        return getData(pid).getLists();
    }

    public void getWriteLock(PageId pid, TransactionId tid) throws TransactionAbortedException {
        var data = getData(pid);
        var lockStatus = getLockStatus(pid);
        LockStatus temp = null;
        boolean has = false;
        for (LockStatus d : lockStatus)
        {
            if (d.tid.equals(tid))
            {
                temp = d;
                has = true;
                break;
            }
        }
        if (has)
        {
            if (temp.isReadLock)
            {
                System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":已有读锁,申请写锁:");
                temp.setWriteLock();
                System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":已有读锁,申请写锁:" + (temp.gettedLock && temp.isWriteLock ? "成功" : "失败"));
                if (!temp.gettedLock)
                {
                    temp.setLockBlock();
                    System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":已有读锁,阻塞申请写锁:" + (temp.gettedLock && temp.isWriteLock ? "成功" : "失败"));
                }
            }
            //            else System.out.println("已有写锁,申请写锁");
        }
        else
        {
            System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,申请写锁:");
            temp = new LockStatus(tid, data);
            temp.setWriteLock();
            data.lists.add(temp);
            System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,申请写锁:" + (temp.gettedLock && temp.isWriteLock ? "成功" : "失败"));
            if (!temp.gettedLock)
            {
                temp.setLockBlock();
                System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,阻塞申请写锁:" + (temp.gettedLock && temp.isWriteLock ? "成功" : "失败"));
            }
        }
        data.lists.set(data.lists.indexOf(temp), temp);
    }

    public void getReadLock(PageId pid, TransactionId tid) throws TransactionAbortedException {
        var data = getData(pid);
        LockStatus temp = null;
        boolean has = false;
        for (LockStatus ls : data.getLists())
        {
            if (ls.tid.equals(tid))
            {
                temp = ls;
                has = true;
                break;
            }
        }
        if (has)
        {
            if (temp.isWriteLock)
            {
                //                System.out.println("已有写锁,申请读锁,测试要求不实现" + "[" + pid.getTableId() % 100 + ":" + pid.getPageNumber() + "]" + Thread.currentThread().getName());
            }
            // else          System.out.println("已有读锁,申请读锁");

        }
        else
        {
            System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,申请读锁:");
            temp = new LockStatus(tid, data);
            temp.setReadLock();
            data.lists.add(temp);
            System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,申请读锁:" + (temp.gettedLock && temp.isReadLock ? "成功" : "失败"));
            if (!temp.gettedLock)
            {
                temp.setLockBlock();
                System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":未持有锁,阻塞申请读锁:" + (temp.gettedLock && temp.isReadLock ? "成功" : "失败"));
            }

        }
        data.lists.set(data.lists.indexOf(temp), temp);
    }

    public void releaseReadWriteLock(PageId pid, TransactionId tid) {
        var data = getData(pid);
        var lockStatus = getLockStatus(pid);
        for (LockStatus ls : lockStatus)
        {
            if (tid.equals(ls.tid))
            {
                if (ls.gettedLock)
                {
                    try
                    {
                        ls.lock.unlock(tid);
                        System.out.println("[" + "pn=" + pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + ":释放" + (ls.gettedLock && ls.isReadLock ? "读锁" : ls.gettedLock && ls.isWriteLock ? "写锁" : "未知锁"));

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                lockStatus.remove(ls);
                break;
            }
        }
    }

    public boolean hasLock(PageId pid, TransactionId tid) {
        var data = getData(pid);
        var lockStatus = getLockStatus(pid);
        for (LockStatus ls : lockStatus)
        {
            if (tid.equals(ls.tid))
            {
                if (ls.gettedLock) return true;
                break;
            }
        }
        return false;
    }

    public Permissions whichLock(PageId pid, TransactionId tid) {
        if (hasLock(pid, tid))
        {
            var data = getData(pid);
            var lockStatus = getLockStatus(pid);
            for (LockStatus ls : lockStatus)
            {
                if (tid.equals(ls.tid))
                {
                    if (ls.gettedLock)
                    {
                        return ls.isReadLock ? Permissions.READ_ONLY : Permissions.READ_WRITE;
                    }
                }
            }

        }
        return null;
    }

    public List<PageId> getPagesByTid(TransactionId tid) {
        List<PageId> pages = new ArrayList<PageId>();
        dataLockMap.values().forEach(e -> {
            e.getLists().forEach(ee -> {
                if (ee.tid.equals(tid)) pages.add(e.getPid());
            });
        });
        return pages;
    }

    private class Data {
        private final ReadWriteLock lock = new FakeReadWriteLock();
        private final ArrayList<LockStatus> lists = new ArrayList<>();
        private PageId pid = null;

        public Data(PageId pid) {
            this.pid = pid;
        }

        public ArrayList<LockStatus> getLists() {
            return lists;
        }

        public PageId getPid() {
            return pid;
        }

        public Lock getRlock() {
            return lock.readLock();
        }

        public Lock getWlock() {
            return lock.writeLock();
        }
    }

    private class LockStatus {
        private TransactionId tid;
        private Data d;
        private Lock lock;
        private boolean isReadLock = false;
        private boolean isWriteLock = false;
        private boolean gettedLock = false;

        public LockStatus(TransactionId tid, Data d) {
            this.tid = tid;
            this.d = d;
        }

        @Override
        public String toString() {
            return "LockStatus{" + "tid=" + tid + (gettedLock ? (isReadLock ? "ReadLock" : isWriteLock ? "WriteLock" : "持有但未知") : "未持有锁") + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LockStatus)) return false;
            LockStatus that = (LockStatus) o;
            return tid.equals(that.tid) && d.equals(that.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tid, d);
        }

        public void setLockBlock() throws TransactionAbortedException {
            if (!gettedLock)
            {
                lock.lock(tid);
                //                gettedLock = true;
                lock.tryLock(tid);
            }
        }

        public synchronized void setReadLock() throws TransactionAbortedException {
            System.out.println("[" + "pn=" + d.pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + "设置读锁前" + d.lock.toString());
            var r = d.getRlock();
            gettedLock = r.tryLock(tid);
            lock = r;
            isReadLock = true;
            isWriteLock = false;
            System.out.println("[" + "pn=" + d.pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + "设置读锁后" + d.lock.toString());

        }

        public synchronized void setWriteLock() throws TransactionAbortedException {
            System.out.println("[" + "pn=" + d.pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + "设置写锁前" + d.lock.toString());
            var w = d.getWlock();
            //单一读锁可升级为写锁
            gettedLock = w.tryLock(tid);
            if (gettedLock)
            {
                lock = w;
                isWriteLock = true;
                isReadLock = false;
            }
            System.out.println("[" + "pn=" + d.pid.getPageNumber() + ":" + "tid=" + tid.getId() % 100 + "]" + Thread.currentThread().getName() + "设置写锁后" + d.lock.toString());
        }

    }

}
