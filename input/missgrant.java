public class missgrant {
  public static void main(String args[]) throws java.io.IOException { 
     new missgrant().run(new java.util.Scanner(System.in), 
                    new java.io.PrintWriter(System.out));
  }
  
  private static final int state$active = 0;
  
  private static final int state$waitingForLight = 1;
  
  private static final int state$waitingForDrawer = 2;
  
  private static final int state$unlockedPanel = 3;
  
  private static final int state$lockedOut = 4;
  
  private static final int state$idle = 5;
  
  private static final int state$idleR = 6;
  
  private static final int state$idleRRR = 7;
  
  private static final int state$idleRR = 8;
  
  void run(java.util.Scanner input, java.io.Writer output) throws java.io.IOException {
    int state = state$active;
    while (true) {
      String token = input.nextLine();
      switch (state) {
        
        case state$active: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight;
          }
          
          if (lightOn(token)) {
             state = state$waitingForDrawer;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForLight: {
          
          
          if (lightOn(token)) {
             state = state$unlockedPanel;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForDrawer: {
          
          
          if (drawerOpened(token)) {
             state = state$unlockedPanel;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$unlockedPanel: {
          
             unlockPanel(output);
          
             lockDoor(output);
          
          
          if (panelClosed(token)) {
             state = state$idle;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$lockedOut: {
          
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$idle: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (lockPanel(token)) {
             state = state$idleR;
          }
          
          break;
        }
        
        case state$idleR: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (lockPanel(token)) {
             state = state$idleRR;
          }
          
          break;
        }
        
        case state$idleRRR: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (lockPanel(token)) {
             state = state$lockedOut;
          }
          
          break;
        }
        
        case state$idleRR: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          if (lockPanel(token)) {
             state = state$idleRRR;
          }
          
          break;
        }
        
      }
    }
  }
  
  private boolean doorClosed(String token) {
    return token.equals("D1CL");
  }
  
  private boolean drawerOpened(String token) {
    return token.equals("D2OP");
  }
  
  private boolean lightOn(String token) {
    return token.equals("L1ON");
  }
  
  private boolean doorOpened(String token) {
    return token.equals("D1OP");
  }
  
  private boolean panelClosed(String token) {
    return token.equals("PNCL");
  }
  
  
  private void unlockPanel(java.io.Writer output) throws java.io.IOException {
    output.write("PNUL\n");
    output.flush();
  }
  
  private void lockPanel(java.io.Writer output) throws java.io.IOException {
    output.write("PNLK\n");
    output.flush();
  }
  
  private void lockDoor(java.io.Writer output) throws java.io.IOException {
    output.write("D1LK\n");
    output.flush();
  }
  
  private void unlockDoor(java.io.Writer output) throws java.io.IOException {
    output.write("D1UL\n");
    output.flush();
  }
  
}