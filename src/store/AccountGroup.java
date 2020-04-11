package store;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;

public class AccountGroup {

    private ArrayList<Account> accounts;
    private LinkedList<Integer> completedQueueIndex;
    private int index = 0;


    public AccountGroup() {
        completedQueueIndex = new LinkedList<>();
        accounts = new ArrayList<>();
    }

    public AccountGroup(ArrayList<Account> accounts) {
        this();
        setAccounts(accounts);
    }


    public void addAccount(Account acc){
        accounts.add(acc);
    }

    public Account getAccount(int i){
        return accounts.get(i);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public void updateCompletedBuildingQueue(){
        Account acc;

        for(int i =0 ; i<accounts.size(); i++){
            acc = accounts.get(i);
            if(acc.isFinishInit()) {
                if (i != index && acc.getError() < 100 && !completedQueueIndex.contains(i) && (
                        acc.getPrimaryHammer().isAvailable() ||
                                (acc.getSecondaryHammer().isAvailable() && !acc.nextBuildingTarget(acc.getPrimaryHammer()).equalsIgnoreCase(""))
                )) {
                    completedQueueIndex.add(i);
                }
            }
        }
    }

    public Account getNextAccount(){
        if(accounts.isEmpty()) return null;
        Account acc;
        acc =  accounts.get(index++);
        if(index >= accounts.size() ){
            index = 0;
        }

        if(acc != null) {
            acc.refreshHammerData();
        }

        return acc;
    }

    public boolean isEmpty(){
        return accounts.isEmpty();
    }


    public void deleteAccount(int i) {
        accounts.remove(i);
    }
    public void deleteAccount(Account acc) {
        accounts.remove(acc);
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }


    public LinkedList<Integer> getCompletedQueueIndex() {
        return completedQueueIndex;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public String[][] getTableData() {
        String[][] data = new String[accounts.size()][Account.Columns.length];

        for(int i =0; i< accounts.size(); i++){
            data[i] = accounts.get(i).getColumnData();
        }

        return data;
    }
}
