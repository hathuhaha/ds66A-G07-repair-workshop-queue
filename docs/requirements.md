# G07 – Repair Workshop Queue

## 1. Problem Context

A repair workshop receives phones, laptops, and small appliances. Staff need to register devices, diagnose faults, estimate repair costs, and move each job through a controlled lifecycle while preserving warranty and customer communication information.

## 2. Final Product

Develop a **repair workshop queue and job tracking application** with the following capabilities:

- Receive and register devices.
- Record symptoms and parts.
- Calculate repair cost estimates.
- Import warranty results from a legacy format.
- Move repair jobs from diagnosis to repair and handover.
- Prevent a job from being closed when approval or payment is missing.

The application should provide the following main views:

- Customers/Devices
- Repair Queue
- Estimate
- Warranty
- Job History

## 3. Learning Focus and Core Model

Focus on the **Adapter design pattern** and **state-changing business behavior**.

Suggested classes:

- `Customer`
- `Device`
- `RepairJob`
- `RepairService`
- `StatusHistory`

## 4. Minimum Functional Requirements

1. Register customers, devices, repair jobs, symptoms, estimates, and status history.
2. Support job states such as `received`, `diagnosing`, `waiting for approval`, `repairing`, `ready`, and `closed`.
3. Calculate an estimate from replaceable parts and labor services.
4. Import a device warranty result from a differently shaped legacy text format through an adapter.
5. Prevent closing a job when the repair is unpaid or required approval is missing.
6. Persist and reload repair jobs, estimates, status history, and warranty results in **PostgreSQL through JDBC**.

## 5. OOP and Acceptance Scenarios

### Design Requirements

- The adapter must isolate the legacy warranty format from the domain model.
- Status changes must validate allowed transitions.

### Demonstration Scenarios

**Demo 1:** Import legacy warranty data and display it through the common warranty interface.

**Demo 2:** Create an estimate and move a job through valid states.

**Demo 3:** Attempt to close a job without approval or payment and show the domain error.

## 6. Optional Extensions

- SMS simulation
- Technician workload
- Printable repair receipt
