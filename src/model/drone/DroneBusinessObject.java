package model.drone;

import controller.EnvironmentController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import model.Hospital;
import util.StopWatch;
import view.CellView;
import view.drone.DroneView;

import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

public class DroneBusinessObject{
    private static StopWatch returnToHomeStopWatch;
    private TimerTask batteryDecrementertimerTask;
    private ScheduledExecutorService batteryDecrementerExecutor;

    private TimerTask goAutomaticDestinyTimerTask;
    private ScheduledExecutorService goAutomaticDestinyExecutor;

    private TimerTask returnToHomeTimerTask;
    private ScheduledExecutorService returnToHomeExecutor;

    private KeyCode currentCommand;
    private static DroneBusinessObject instance;

    private DroneBusinessObject() {

    }

    public static DroneBusinessObject getInstance(){
        if(instance == null){
            instance = new DroneBusinessObject();
        }

        return instance;
    }





    /*Drone Normal Behaviors*/
    public static boolean takeOff(Drone selectedDrone) {
        if(selectedDrone.isTookOff()){
            return false;
        }

        selectedDrone.setIsTookOff(true);

        return true;
    }

    public static boolean landing(Drone selectedDrone) {

        selectedDrone.setIsTookOff(false);

        return true;

    }

    public static boolean shutDown(Drone selectedDrone){

        if(!selectedDrone.isStarted()){
            return false;
        }

        if(selectedDrone.isTookOff()){
            return false;
        }

        selectedDrone.setStarted(false);
/*
        stopBatteryDecrementer();
        stopReturnToHome();
        stopGoAutomaticDestiny();*/

        return true;
    }

    public static boolean start(Drone selectedDrone){

        if(selectedDrone.isStarted()){
            return false;
        }

        if(selectedDrone.isTookOff()){
            return false;
        }


        selectedDrone.setStarted(true);


       /* stopBatteryDecrementer();
        startBatteryDecrementer();
*/
        return true;
    }

    public static void notifyRunEnviroment(Drone currentDrone) {

    }

    public static void notifyStopEnviroment(Drone currentDrone) {

    }

    public synchronized static void updateBatteryPerBlock(Drone drone) {

        if(drone.isShutDown()){
            return;
        }

        if(drone.isEconomyMode()){

            Double oldCurrentBattery = drone.getCurrentBattery();
            Double batteryPerBlock = drone.getBatteryPerBlock();

            Double economyModeBatteryPerBlock = batteryPerBlock/2;

            Double newCurrentBattery = oldCurrentBattery-economyModeBatteryPerBlock;

            drone.setCurrentBattery(newCurrentBattery);

        }else if(drone.isNormalMode()){
            Double oldCurrentBattery = drone.getCurrentBattery();
            Double batteryPerBlock = drone.getBatteryPerBlock();
            Double newCurrentBattery = oldCurrentBattery-batteryPerBlock;

            drone.setCurrentBattery(newCurrentBattery);
        }

    }

    public synchronized static void updateBatteryPerSecond(Drone drone) {

        if(drone.isShutDown()){
            return;
        }

        if(drone.isEconomyMode()){

            Double oldCurrentBattery = drone.getCurrentBattery();
            Double batteryPerSecond = drone.getBatteryPerSecond();

            Double economyModeBatteryPerSecond = batteryPerSecond/2;

            Double newCurrentBattery = oldCurrentBattery-economyModeBatteryPerSecond;

            drone.setCurrentBattery(newCurrentBattery);

        }else if(drone.isNormalMode()){
            Double oldCurrentBattery = drone.getCurrentBattery();
            Double batteryPerSecond = drone.getBatteryPerSecond();
            Double newCurrentBattery = oldCurrentBattery-batteryPerSecond;

            drone.setCurrentBattery(newCurrentBattery);
        }

    }

    public static boolean safeLanding(Drone selectedDrone) {
        selectedDrone.setIsSafeland(true);
        selectedDrone.setIsTookOff(false);

        return shutDown(selectedDrone);

    }




    public synchronized static void checkStatus(Drone selectedDrone) {
        System.out.println("checkStatus");
        System.out.println(selectedDrone.toString());

        KeyCode flyDirectionCommand = selectedDrone.getFlyDirectionCommand();

              if(selectedDrone.getCurrentBattery()>10 && selectedDrone.getDistanceHospitalDestiny()>0 && flyDirectionCommand != null
                && !selectedDrone.isReturningToHome()
                && !selectedDrone.isSafeLand()
                && !selectedDrone.isBadConnection()
                && !selectedDrone.isSafeLand()){

                flying(selectedDrone, flyDirectionCommand);

                selectedDrone.setFlyDirectionCommand(null);

        }

        if(selectedDrone.isBadConnection()
                && !selectedDrone.isReturningToHome()){

            returnToHome(selectedDrone);

            selectedDrone.setReturningToHome(true);

        }

        if(selectedDrone.isBadConnection() && selectedDrone.isReturningToHome()
                && selectedDrone.getDistanceHospitalSource() == 0){

           // stopReturnToHome();
            returnToHomeStopWatch.stop();
            landing(selectedDrone);

            shutDown(selectedDrone);

            selectedDrone.setGoingAutomaticToDestiny(false);
            selectedDrone.setGoingManualToDestiny(false);


        }

        if(selectedDrone.getCurrentBattery() <= 15 && selectedDrone.isNormalMode()){
            applyEconomyMode(selectedDrone);
        }

        if(selectedDrone.getCurrentBattery() <= 10 && selectedDrone.getDistanceHospitalDestiny()>0
                && !selectedDrone.isSafeLand()){

          //  stopGoAutomaticDestiny();

            returnToHomeStopWatch.stop();


            selectedDrone.setGoingAutomaticToDestiny(false);
            selectedDrone.setGoingManualToDestiny(false);


            safeLanding(selectedDrone);





        }

        if(selectedDrone.getDistanceHospitalDestiny() == 0){

            // stopGoAutomaticDestiny();

            selectedDrone.setGoingAutomaticToDestiny(false);
            selectedDrone.setGoingManualToDestiny(false);

            landing(selectedDrone);

            shutDown(selectedDrone);


        }


    }

    public static void flyingDown(Drone selectedDrone) {

        EnvironmentController environmentController = EnvironmentController.getInstance();

        int newI =  selectedDrone.getCurrentPositionI();
        int newJ = selectedDrone.getCurrentPositionJ();
        newI = newI+1;
        int minIInEnverionment = 0;
        int maxIInEnverionment = environmentController.getCountRow()-1;

        //todo eu acho que essa checagem não deveria estar aqui ach oque era no environmentController
        if(newI > maxIInEnverionment || newI < minIInEnverionment){

            return;
        }


        selectedDrone.setCurrentPositionI(newI);
    }

    public static void flyingUp(Drone selectedDrone) {

        EnvironmentController environmentController = EnvironmentController.getInstance();

        int newI =  selectedDrone.getCurrentPositionI();
        int newJ = selectedDrone.getCurrentPositionJ();
        newI = newI-1;

        int minIInEnverionment = 0;
        int maxIInEnverionment = environmentController.getCountRow()-1;


        if(newI > maxIInEnverionment || newI < minIInEnverionment){
            return;
        }

        selectedDrone.setCurrentPositionI(newI);
    }

    public static void flyingRight(Drone selectedDrone) {

        EnvironmentController environmentController = EnvironmentController.getInstance();

        int newI =  selectedDrone.getCurrentPositionI();
        int newJ = selectedDrone.getCurrentPositionJ();
        newJ = newJ +1;

        int minJInEnverionment = 0;
        int maxJInEnverionment = environmentController.getCountCollumn()-1;

        if(newJ>maxJInEnverionment  || newJ < minJInEnverionment){

            return;
        }

        selectedDrone.setCurrentPositionJ(newJ);
    }

    public static void flyingLeft(Drone selectedDrone) {

        EnvironmentController environmentController = EnvironmentController.getInstance();

        int newI =  selectedDrone.getCurrentPositionI();
        int newJ = selectedDrone.getCurrentPositionJ();
        newJ = newJ -1;

        int minJInEnverionment = 0;
        int maxJInEnverionment = environmentController.getCountCollumn()-1;


        if(newJ > maxJInEnverionment || newJ < minJInEnverionment){

            return;
        }

        selectedDrone.setCurrentPositionJ(newJ);
    }


    public static void updateDistances(Drone selectedDrone) {
        updateDistanceHospitalSource(selectedDrone);
        updateDistanceHospitalDestiny(selectedDrone);
    }

    static synchronized public void updateDistanceHospitalDestiny(Drone selectedDrone) {
        double distanceHospitalDestiny = calculeteDistanceFrom(selectedDrone, selectedDrone.getDestinyHopistal());
          System.out.println("distanceHospitalDestiny"+ distanceHospitalDestiny);


        selectedDrone.setDistanceHospitalDestiny(distanceHospitalDestiny);
    }

    static synchronized public void updateDistanceHospitalSource(Drone selectedDrone) {
        double distanceHospitalSource = calculeteDistanceFrom(selectedDrone, selectedDrone.getSourceHospital());
        System.out.println("distanceHospitalSource"+ distanceHospitalSource);
        selectedDrone.setDistanceHospitalSource(distanceHospitalSource);
    }

     public static double calculeteDistanceFrom(Drone selectedDrone, Hospital hospital) {

        int xInitial = (selectedDrone.getCurrentPositionJ()+1)*30,
                xFinal= (hospital.getjPosition()+1)*30,
                yInitial = (selectedDrone.getCurrentPositionI()+1)*30,
                yFinal =(hospital.getiPosition()+1)*30;

        return Math.sqrt(((xFinal-xInitial)*(xFinal-xInitial)) + ((yFinal- yInitial)*(yFinal- yInitial)));

    }

/*    synchronized public void updateBattery(Drone selectedDrone) {
        double newValueBattery;
        if(selectedDrone.isEconomyMode()){
            newValueBattery = selectedDrone.getCurrentBattery()-(selectedDrone.getBatteryPerBlock()/2);
        }else {
            newValueBattery = selectedDrone.getCurrentBattery()-(selectedDrone.getBatteryPerBlock());
        }

        selectedDrone.setCurrentBattery(newValueBattery);
    }*/

    public static void flying(Drone selectedDrone, KeyCode flyDirectionCommand) {
        System.out.println("flying");
        //System.out.println("Drone["+drone.getUniqueID()+"] "+"Flying");
       // loggerController.print("Drone["+drone.getUniqueID()+"] "+"Flying");

        // irregular moviments
        if(selectedDrone.isEconomyMode()){
            Random random = new Random();
            double value = random.nextDouble();

            // right moviments
            if(value>0.8){
                if(flyDirectionCommand == KeyCode.D){
                    flyingRight(selectedDrone);
                }
                else if(flyDirectionCommand == KeyCode.A){
                    flyingLeft(selectedDrone);
                }
                else if(flyDirectionCommand == KeyCode.W){
                    flyingUp(selectedDrone);
                }
                else if(flyDirectionCommand == KeyCode.S){
                    flyingDown(selectedDrone);
                }
            }else {
                //wrong moviments

                int randomNum = 0 + (int) (Math.random() * 4);
                System.out.println("Random number " + randomNum);

                if(randomNum==0){
                    flyingLeft(selectedDrone);
                }
                else if(randomNum==1){
                    flyingLeft(selectedDrone);
                }
                else if(randomNum==2){

                }
                else if(randomNum==3){

                }
            }



        }else {
            // normal moviment
            if(flyDirectionCommand == KeyCode.D){
                flyingRight(selectedDrone);
            }
            else if(flyDirectionCommand == KeyCode.A){
                flyingLeft(selectedDrone);
            }
            else if(flyDirectionCommand == KeyCode.W){
                flyingUp(selectedDrone);
            }
            else if(flyDirectionCommand == KeyCode.S){
                flyingDown(selectedDrone);
            }
        }


        updateDistances(selectedDrone);
        updateBatteryPerSecond(selectedDrone);
        updateItIsOver(selectedDrone);


    }

    public static Node updateItIsOver(Drone drone) {


        CellView currentCellView = (CellView) EnvironmentController.getInstance().getCellViewSelected();
        DroneView droneView = EnvironmentController.getInstance().getDroneViewMap().get(drone.getUniqueID());

        if(currentCellView == null){
            return null;
        }

        drone.getOnTopOfList().clear();



        for(Node node : currentCellView.getChildren()){

            if(node==droneView){
                continue;
            }

            drone.addOnTopOfDroneList(node);
        }
        if(!drone.getOnTopOfList().isEmpty()){
            //System.out.println(drone.getOnTopOfList().get(drone.getOnTopOfList().size()-1));
        }


        return null;
    }

    /*public void goDestinyAutomatic() {
        drone.setGoingAutomaticToDestiny(true);

        start();
        stopBatteryDecrementer();
        startBatteryDecrementer();
        takeOff();



        *//* goAutomaticDestinyTimer = new Timer();*//*
        goAutomaticDestinyTimerTask = new TimerTask() {
            @Override
            public void run() {

                Platform.runLater(() -> {
                    try {


                        int oldI = drone.getCurrentPositionI();
                        int oldJ = drone.getCurrentPositionJ();
                        double newDistanceDestiny = 999999;
                        String mustGO = null;

                        double tempDistance = distanceDroneWentRight(drone.getDestinyHopistal());

                        if(tempDistance < newDistanceDestiny){
                            newDistanceDestiny = tempDistance;
                            mustGO ="->";
                        }

                        drone.setCurrentPositionI(oldI);
                        drone.setCurrentPositionJ(oldJ);

                        tempDistance = distanceDroneWentLeft(drone.getDestinyHopistal());

                        if(tempDistance<newDistanceDestiny){
                            newDistanceDestiny = tempDistance;
                            mustGO ="<-";
                        }

                        drone.setCurrentPositionI(oldI);
                        drone.setCurrentPositionJ(oldJ);

                        tempDistance = distanceDroneWentUp(drone.getDestinyHopistal());

                        if(tempDistance<newDistanceDestiny){
                            newDistanceDestiny = tempDistance;
                            mustGO ="/\\";

                        }

                        drone.setCurrentPositionI(oldI);
                        drone.setCurrentPositionJ(oldJ);

                        tempDistance = distanceDroneWentDown(drone.getDestinyHopistal());

                        if(tempDistance<newDistanceDestiny){
                            newDistanceDestiny = tempDistance;
                            mustGO ="\\/";

                        }

                        drone.setCurrentPositionI(oldI);
                        drone.setCurrentPositionJ(oldJ);



                        goTo(mustGO);



                        updateBattery();

                        updateItIsOver();

                        checkStatus();


                    }catch (Exception e){
                        System.out.println();
                    }
                });
            }
        };


        *//*    goAutomaticDestinyTimer.scheduleAtFixedRate(goAutomaticDestinyTimerTask,0,1000);*//*

        goAutomaticDestinyExecutor = Executors.newSingleThreadScheduledExecutor();
        goAutomaticDestinyExecutor.scheduleAtFixedRate(goAutomaticDestinyTimerTask, 0, 1000, TimeUnit.MILLISECONDS);



    }*/



    public static void returnToHome(Drone selectedDrone){


        System.out.println("returnToHome");
      Runnable runnable = () -> Platform.runLater(() -> {
          int oldI = selectedDrone.getCurrentPositionI();
          int oldJ = selectedDrone.getCurrentPositionJ();
          double newDistanceSource = 999999;
          String mustGO = null;

          double tempDistance = distanceDroneWentRight(selectedDrone, selectedDrone.getSourceHospital());

          if(tempDistance < newDistanceSource){
              newDistanceSource = tempDistance;
              mustGO ="->";
          }

          selectedDrone.setCurrentPositionI(oldI);
          selectedDrone.setCurrentPositionJ(oldJ);

          tempDistance = distanceDroneWentLeft(selectedDrone, selectedDrone.getSourceHospital());

          if(tempDistance<newDistanceSource){
              newDistanceSource = tempDistance;
              mustGO ="<-";
          }

          selectedDrone.setCurrentPositionI(oldI);
          selectedDrone.setCurrentPositionJ(oldJ);

          tempDistance = distanceDroneWentUp(selectedDrone, selectedDrone.getSourceHospital());

          if(tempDistance<newDistanceSource){
              newDistanceSource = tempDistance;
              mustGO ="/\\";

          }

          selectedDrone.setCurrentPositionI(oldI);
          selectedDrone.setCurrentPositionJ(oldJ);

          tempDistance = distanceDroneWentDown(selectedDrone, selectedDrone.getSourceHospital());

          if(tempDistance<newDistanceSource){
              newDistanceSource = tempDistance;
              mustGO ="\\/";

          }

          selectedDrone.setCurrentPositionI(oldI);
          selectedDrone.setCurrentPositionJ(oldJ);


          goTo(selectedDrone, mustGO);

          checkStatus(selectedDrone);
          updateDistances(selectedDrone);
          updateItIsOver(selectedDrone);

      });

        returnToHomeStopWatch = new StopWatch(0,1500, runnable);
        returnToHomeStopWatch.start();


    }


/*    public static void returnToHome(Drone selectedDrone){


        selectedDrone.setReturningToHome(true);

        *//* returnToHomeTimer = new Timer();*//*
        returnToHomeTimerTask = new TimerTask() {
            @Override
            public void run() {

                Platform.runLater(() -> {


                    int oldI = selectedDrone.getCurrentPositionI();
                    int oldJ = selectedDrone.getCurrentPositionJ();
                    double newDistanceSource = 999999;
                    String mustGO = null;

                    double tempDistance = distanceDroneWentRight(selectedDrone, selectedDrone.getSourceHospital());

                    if(tempDistance < newDistanceSource){
                        newDistanceSource = tempDistance;
                        mustGO ="->";
                    }

                    selectedDrone.setCurrentPositionI(oldI);
                    selectedDrone.setCurrentPositionJ(oldJ);

                    tempDistance = distanceDroneWentLeft(selectedDrone, selectedDrone.getSourceHospital());

                    if(tempDistance<newDistanceSource){
                        newDistanceSource = tempDistance;
                        mustGO ="<-";
                    }

                    selectedDrone.setCurrentPositionI(oldI);
                    selectedDrone.setCurrentPositionJ(oldJ);

                    tempDistance = distanceDroneWentUp(selectedDrone, selectedDrone.getSourceHospital());

                    if(tempDistance<newDistanceSource){
                        newDistanceSource = tempDistance;
                        mustGO ="/\\";

                    }

                    selectedDrone.setCurrentPositionI(oldI);
                    selectedDrone.setCurrentPositionJ(oldJ);

                    tempDistance = distanceDroneWentDown(selectedDrone, selectedDrone.getSourceHospital());

                    if(tempDistance<newDistanceSource){
                        newDistanceSource = tempDistance;
                        mustGO ="\\/";

                    }

                    selectedDrone.setCurrentPositionI(oldI);
                    selectedDrone.setCurrentPositionJ(oldJ);


                    goTo(selectedDrone, mustGO);


                    checkStatus(selectedDrone);



                });

            }
        };


        *//*returnToHomeTimer.scheduleAtFixedRate(returnToHomeTimerTask,0,1000);*//*
        returnToHomeExecutor = Executors.newSingleThreadScheduledExecutor();
        returnToHomeExecutor.scheduleAtFixedRate(returnToHomeTimerTask, 0, 1000, TimeUnit.MILLISECONDS);



    }*/

    public static void goTo(Drone drone, String mustGO) {

        //irregular moviments
        if(drone.isEconomyMode()){
            Random random = new Random();
            double value = random.nextDouble();

            // right moviments
            if(value>0.8){
                switch (mustGO){
                    case "->":
                        flyingRight(drone);
                        break;

                    case "<-":
                        flyingLeft(drone);
                        break;

                    case "/\\":
                        flyingUp(drone);
                        break;

                    case "\\/":
                        flyingDown(drone);
                        break;
                }
            }else {

                //wrong moviments

                int randomNum = 0 + (int) (Math.random() * 4);

                switch (randomNum){
                    case 0:
                        flyingLeft(drone);
                        break;

                    case 1:
                        flyingRight(drone);
                        break;

                    case 2:

                        break;

                    case 3:

                        break;
                }

            }

            // normal moviments
        }else {
            switch (mustGO){
                case "->":
                    flyingRight(drone);
                    break;

                case "<-":
                    flyingLeft(drone);
                    break;

                case "/\\":
                    flyingUp(drone);
                    break;

                case "\\/":
                    flyingDown(drone);
                    break;
            }

        }


    }

    public static double distanceDroneWentUp(Drone drone, Hospital hospital) {
        drone.setCurrentPositionI(drone.getCurrentPositionI()-1);

        if(drone.getCurrentPositionI()<0){
            return 999999;
        }

        return calculeteDistanceFrom(drone,hospital);
    }

    public static double distanceDroneWentDown(Drone drone, Hospital hospital) {
        drone.setCurrentPositionI(drone.getCurrentPositionI()+1);

        if(drone.getCurrentPositionI()<0){
            return 999999;
        }

        return calculeteDistanceFrom(drone,hospital);
    }

    public static double distanceDroneWentLeft(Drone drone, Hospital hospital) {
        drone.setCurrentPositionJ(drone.getCurrentPositionJ()-1);

        if(drone.getCurrentPositionJ()<0){
            return 999999;
        }

        return calculeteDistanceFrom(drone,hospital);
    }

    public static double distanceDroneWentRight(Drone drone, Hospital hospital) {

        if(drone.getCurrentPositionJ()<0){
            return 999999;
        }

        drone.setCurrentPositionJ(drone.getCurrentPositionJ()+1);

        return calculeteDistanceFrom(drone,hospital);
    }


    public static void applyEconomyMode(Drone drone) {

        if(drone.isEconomyMode()){
            return;
        }

        drone.setEconomyMode(true);

       /* System.out.println("Drone["+drone.getUniqueID()+"] "+"Start Economy Mode");
        loggerController.print("Drone["+drone.getUniqueID()+"] "+"Start Economy Mode");*/
    }



    public static void resetSettingsDrone(Drone currentDrone) {
        currentDrone.setCurrentBattery(currentDrone.getInitialBattery());
        currentDrone.setCurrentPositionI(currentDrone.getInitialPosistionI());
        currentDrone.setCurrentPositionJ(currentDrone.getInitialPositionJ());
        currentDrone.setReturningToHome(false);
        currentDrone.setBadConnection(false);
        currentDrone.setEconomyMode(false);
        currentDrone.setGoingAutomaticToDestiny(false);
        currentDrone.setGoingManualToDestiny(false);
        currentDrone.setIsSafeland(false);
        currentDrone.setIsTookOff(false);
        currentDrone.setStarted(false);

        if(returnToHomeStopWatch != null){
            returnToHomeStopWatch.stop();
        }

        //stopGoAutomaticDestiny();
       // stopReturnToHome();


    }

    public static void setBadConnection(Drone selectedDrone) {
        selectedDrone.setBadConnection(true);
    }

    public static void setNormalConnection(Drone selectedDrone) {
        selectedDrone.setBadConnection(false);
    }

    public static void setStrongWind(Drone currentDrone) {
        currentDrone.setStrongWind(true);
    }

    public static void setNormalWind(Drone currentDrone) {
        currentDrone.setStrongWind(false);
    }

    public static void updateFlyDirectionCommand(KeyCode flyDirectionCommand, Drone selectedDrone) {
        selectedDrone.setFlyDirectionCommand(flyDirectionCommand);
    }
}
