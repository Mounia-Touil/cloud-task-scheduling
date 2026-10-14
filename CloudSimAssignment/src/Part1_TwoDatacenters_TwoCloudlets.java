// Class:  CS7125
// Term:  Spring 2026
// Name: Mounia Touil
// Program Number: Assignment 1_Create Data Center, Host, Virtual Machine, and Cloudlet
// IDE: Eclipse

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.text.DecimalFormat;
import java.util.*;

public class Part1_TwoDatacenters_TwoCloudlets {

    public static void main(String[] args) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            // Two datacenters, each with one host
            createDatacenter("Datacenter_1");
            createDatacenter("Datacenter_2");

            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            // Two VMs with different MIPS
            List<Vm> vmlist = new ArrayList<>();
            int pesNumber = 1;
            int ram = 1024;
            long bw = 1000;
            long size = 10000;
            String vmm = "Xen";

            Vm vmFast = new Vm(0, brokerId, 1000, pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerTimeShared());
            Vm vmSlow = new Vm(1, brokerId, 500, pesNumber, ram, bw, size, vmm,
                    new CloudletSchedulerTimeShared());

            vmlist.add(vmFast);
            vmlist.add(vmSlow);
            broker.submitVmList(vmlist);

            // Two cloudlets (same length)
            List<Cloudlet> cloudletList = new ArrayList<>();
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel um = new UtilizationModelFull();

            Cloudlet c1 = new Cloudlet(0, length, pesNumber, fileSize, outputSize, um, um, um);
            c1.setUserId(brokerId);
            Cloudlet c2 = new Cloudlet(1, length, pesNumber, fileSize, outputSize, um, um, um);
            c2.setUserId(brokerId);

            cloudletList.add(c1);
            cloudletList.add(c2);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();
            List<Cloudlet> results = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(results);

            System.out.println("\nExpected (rough):");
            System.out.println("On 1000 MIPS: ~40000/1000 = 40 sec");
            System.out.println("On  500 MIPS: ~40000/ 500 = 80 sec");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        // One host per datacenter
        int hostRam = 4096;
        long storage = 1000000;
        long bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(2000)));
        peList.add(new Pe(1, new PeProvisionerSimple(2000)));

        Host host = new Host(0,
                new RamProvisionerSimple(hostRam),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList));

        hostList.add(host);

        DatacenterCharacteristics ch = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList,
                10.0, 0.01, 0.005, 0.001, 0.0
        );

        try {
            return new Datacenter(name, ch, new VmAllocationPolicySimple(hostList),
                    new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println("\n========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent
                + "Datacenter ID" + indent + "VM ID" + indent
                + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet cloudlet : list) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                System.out.println(indent + cloudlet.getCloudletId()
                        + indent + "SUCCESS"
                        + indent + indent + cloudlet.getResourceId()
                        + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime())
                        + indent + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}
