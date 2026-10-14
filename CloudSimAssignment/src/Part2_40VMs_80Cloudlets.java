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

public class Part2_40VMs_80Cloudlets {

    public static void main(String[] args) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            // Datacenter with multiple hosts so it can fit 40 VMs
            Datacenter dc = createDatacenterWithHosts("Datacenter_Scalable", 5);

            DatacenterBroker broker = new DatacenterBroker("Broker_Scalable");
            int brokerId = broker.getId();

            // 1) Create 40 VMs
            List<Vm> vmlist = new ArrayList<>();
            int pesNumber = 1;
            int ram = 512;
            long bw = 1000;
            long size = 10000;
            String vmm = "Xen";

            int[] mipsChoices = {500, 750, 1000, 1250};

            for (int i = 0; i < 40; i++) {
                int mips = mipsChoices[i % mipsChoices.length];
                Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                        new CloudletSchedulerTimeShared());
                vmlist.add(vm);
            }
            broker.submitVmList(vmlist);

            // 2) Create 80 cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();
            UtilizationModel utilizationModel = new UtilizationModelFull();

            for (int i = 0; i < 80; i++) {
                long length = 20000 + (i % 10) * 2000; // 20000..38000
                Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, 300, 300,
                        utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
            }
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();
            List<Cloudlet> results = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(results);

            System.out.println("\n===== SUMMARY =====");
            System.out.println("Total VMs: 40");
            System.out.println("Total Cloudlets: 80");
            System.out.println("Since Cloudlets > VMs, some cloudlets start later (queue).");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenterWithHosts(String name, int hostCount) {
        List<Host> hostList = new ArrayList<>();

        int hostMipsPerCore = 2000;
        int hostPes = 8;        // cores per host
        int hostRam = 16384;    // MB
        long hostStorage = 1000000;
        long hostBw = 20000;

        for (int h = 0; h < hostCount; h++) {
            List<Pe> peList = new ArrayList<>();
            for (int p = 0; p < hostPes; p++) {
                peList.add(new Pe(p, new PeProvisionerSimple(hostMipsPerCore)));
            }

            Host host = new Host(
                    h,
                    new RamProvisionerSimple(hostRam),
                    new BwProvisionerSimple(hostBw),
                    hostStorage,
                    peList,
                    new VmSchedulerTimeShared(peList)
            );
            hostList.add(host);
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList,
                10.0, 0.01, 0.005, 0.001, 0.0
        );

        try {
            return new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),
                    new LinkedList<Storage>(),
                    0
            );
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
