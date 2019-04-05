package protocol.subprotocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    void backup(String[] cmd) throws RemoteException;
    void restore(String[] cmd) throws RemoteException;
    void delete(String[] cmd) throws RemoteException;
    void reclaim(String[] cmd) throws RemoteException;
    void state(String[] cmd) throws RemoteException;
}
