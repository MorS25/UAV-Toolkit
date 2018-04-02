package uav.generic.struct.states;

/**
 * Classe que modela os possíveis estados do sistema como um todo.
 * @author Jesimar S. Arantes
 */
public enum StateSystem {
    
    INITIALIZING, INITIALIZED, DISABLED;
    
    public static int getMode(StateSystem mode){
        switch (mode) {
            case INITIALIZING:
                return 0;
            case INITIALIZED:
                return 1;
            case DISABLED:
                return 2;            
            default:
                return -1;
        }
    }
}
