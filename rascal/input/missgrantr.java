public class missgrantr {
  public static void main(String args[]) throws java.io.IOException { 
     new missgrantr().run(new java.util.Scanner(System.in), 
                    new java.io.PrintWriter(System.out));
  }
  
  private static final int state$idle = 0;
  
  private static final int state$active = 1;
  
  private static final int state$waitingForLight = 2;
  
  private static final int state$waitingForDrawer = 3;
  
  private static final int state$unlockedPanel = 4;
  
  private static final int state$active_1 = 5;
  
  private static final int state$active_2 = 6;
  
  private static final int state$active_3 = 7;
  
  private static final int state$active_4 = 8;
  
  void run(java.util.Scanner input, java.io.Writer output) throws java.io.IOException {
    int state = state$idle;
    while (true) {
      String token = input.nextLine();
      switch (state) {
        
        case state$idle: {
          
             unlockDoor(output);
          
             lockPanel(output);
          
          
          if (doorClosed(token)) {
             state = state$active_4;
          }
          
          if (doorOpened(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$active: {
          
          
          if (drawerOpened(token)) {
             state = state$waitingForLight;
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
        
        case state$active_1: {
          
          
          if (lightOn(token)) {
             state = state$waitingForDrawer;
          }
          
          break;
        }
        
        case state$active_2: {
          
          
          if (lightOn(token)) {
             state = state$active_1;
          }
          
          break;
        }
        
        case state$active_3: {
          
          
          if (lightOn(token)) {
             state = state$active_2;
          }
          
          break;
        }
        
        case state$active_4: {
          
          
          if (lightOn(token)) {
             state = state$active_3;
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