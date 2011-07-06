public class ControllerMethods {
  
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
  
  
  private void unlockPanel(Writer output) {
    output.write("PNUL");
  }
  
  private void lockPanel(Writer output) {
    output.write("PNLK");
  }
  
  private void lockDoor(Writer output) {
    output.write("D1LK");
  }
  
  private void unlockDoor(Writer output) {
    output.write("D1UL");
  }
  
  
  private void state$idle(Scanner input, Writer output) {
    
    unlockDoor(output);
    
    lockPanel(output);
    
    String token = input.nextLine();
    
    if (doorClosed(token)) {
       state$active(input, output);
       return;
    }
    
    if (doorOpened(token)) {
       state$idle(input, output);
       return;
    }
    
  }
  
  private void state$active(Scanner input, Writer output) {
    
    String token = input.nextLine();
    
    if (drawerOpened(token)) {
       state$waitingForLight(input, output);
       return;
    }
    
    if (lightOn(token)) {
       state$waitingForDrawer(input, output);
       return;
    }
    
    if (doorOpened(token)) {
       state$idle(input, output);
       return;
    }
    
  }
  
  private void state$waitingForLight(Scanner input, Writer output) {
    
    String token = input.nextLine();
    
    if (lightOn(token)) {
       state$unlockedPanel(input, output);
       return;
    }
    
    if (doorOpened(token)) {
       state$idle(input, output);
       return;
    }
    
  }
  
  private void state$waitingForDrawer(Scanner input, Writer output) {
    
    String token = input.nextLine();
    
    if (drawerOpened(token)) {
       state$unlockedPanel(input, output);
       return;
    }
    
    if (doorOpened(token)) {
       state$idle(input, output);
       return;
    }
    
  }
  
  private void state$unlockedPanel(Scanner input, Writer output) {
    
    unlockPanel(output);
    
    lockDoor(output);
    
    String token = input.nextLine();
    
    if (panelClosed(token)) {
       state$idle(input, output);
       return;
    }
    
    if (doorOpened(token)) {
       state$idle(input, output);
       return;
    }
    
  }
  
}