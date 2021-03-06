package s.w.os;

import javax.swing.JMenuItem;

//STCF will run the PCB's according to time remaining, and will interupt a running PCB when needed
public class STCFScheduler extends JMenuItem implements CommandPCB
{
    @Override
    public PCBList execute(PCBList list)
    {
        PCBParser parser = new PCBParser(); //create the parser
        parser.getFileName();//get the file name
        
        if (parser.quit) //if the user said to quit, stop everything in the command
        {
            return list;
        }
        
        //get first line of the file
        parser.readNextLine(); //get next line of data for the PCB
        
        //run the scheduler
        while (true)
        {
            //if all PCB's are finished, and there is no more input from the file, quit
            if (list.readyQueue.size() <= 0 && list.runningQueue.size() <= 0 && parser.doneWFile)
            {
                break;
            }
            
            //if it is time for last PCB read to enter
            if (list.runningQueue.totalTime == parser.getNextTOA())
            {
                int actualClass = 0; //variable that used in making the PCB

                //stuff to add to the PCB being created
                String processName = parser.processNameTOA.get(parser.getNextTOA());
                Character pclass = parser.Classes.get(processName);
                switch(pclass)
                {
                    case 'A':
                        actualClass = 0;
                        break;
                    case 'S':
                        actualClass = 1;
                        break;
                }

                int priority = parser.priority.get(processName);
                int memory = parser.memory.get(processName);
                int timeOfArrival = parser.timeOfArrival.get(processName);
                int timeRemaining = parser.timeRemaining.get(processName);
                int CPU = parser.CPU.get(processName); 
                    
                //make the PCB and shove it onto the ready queue
                list.SetupPCB(processName, actualClass, priority, timeRemaining, memory, timeOfArrival, CPU);
                    
                parser.readNextLine(); //get next line of data for the PCB
            }
            
            //insert PCBs if there are PCBs to insert
            while (list.readyQueue.size() > 0)
            {
                //if the PCB fits, remove it from the ready queue, otherwise exit
                if (list.runningQueue.insertPCB((PCB)list.readyQueue.get(0)))
                {
                    list.readyQueue.remove(0);
                }
                else 
                {
                    break;
                }
            }
            
            //if there are things to compare for interupt, compare them
            if (list.runningQueue.size() > 0 && list.readyQueue.size() > 0)
            {
                //temporary PCBs for comparison
                int index = 0; //start comparisons at the first position
                
                //look through the entire running queue for an interruptable PCB
                while(index < list.runningQueue.size())
                {
                    PCB runningPCB = (PCB)list.runningQueue.get(index);
                    PCB readyPCB = (PCB)list.readyQueue.get(0);

                    //if the PCB on the readyQueue's front has a lower time remaining than the running PCB
                    //and it will fit in the memory, swap them
                    if (readyPCB.timeRemaining < runningPCB.timeRemaining)
                    {
                        //if it will fit once interrupted
                        if (runningPCB.memoryValue + list.runningQueue.getFreeMemory() 
                                >= readyPCB.memoryValue)
                        {
                            //put the interrupted PCB in the ready
                            list.readyQueue.insertPCB(list.runningQueue.removeRunningPCB(runningPCB));
                            
                            //insert the ready PCB into the running queue
                            list.runningQueue.insertPCB(readyPCB);
                            
                            //remove the PCB in the ready
                            list.readyQueue.remove(0);
                        }
                    }
                    
                    index++; //increment index on the running queue
                }
            }
            
            list.runningQueue.timeCycle(); //run the PCB for one time cycle
        }
        
        //do final stuff (output end results and return the list)
        list.runningQueue.outputEnd();
        list.runningQueue.resetMemory();
        return list;
    }
}
