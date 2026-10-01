let parkedVehicles = {};

function parkVehicle() {

    const vehicleNumber =
        document.getElementById("parkNumber").value
            .trim()
            .toUpperCase();

    const vehicleType =
        document.getElementById("vehicleType").value;

    if (vehicleNumber === "") {
        showMessage("Please enter a vehicle number.");
        return;
    }

    if (parkedVehicles[vehicleNumber]) {
        showMessage("Vehicle is already parked.");
        return;
    }

    const prefix =
        vehicleType === "CAR" ? "C" : "B";

    const slots =
        document.querySelectorAll(
            "#" +
            (vehicleType === "CAR" ? "carSlots" : "bikeSlots") +
            " .slot"
        );

    let selectedSlot = null;

    for (const slot of slots) {

        if (slot.classList.contains("available")) {

            selectedSlot = slot;
            break;
        }
    }

    if (!selectedSlot) {

        showMessage(
            "No available " +
            vehicleType.toLowerCase() +
            " slots."
        );

        return;
    }

    selectedSlot.classList.remove("available");
    selectedSlot.classList.add("occupied");

    parkedVehicles[vehicleNumber] = {
        slot: selectedSlot.dataset.slot,
        type: vehicleType,
        entryTime: new Date()
    };

    updateAvailableCount();

    showMessage(
        vehicleNumber +
        " parked successfully in slot " +
        selectedSlot.dataset.slot +
        "."
    );

    document.getElementById("parkNumber").value = "";
}

function exitVehicle() {

    const vehicleNumber =
        document.getElementById("exitNumber").value
            .trim()
            .toUpperCase();

    if (vehicleNumber === "") {
        showMessage("Please enter a vehicle number.");
        return;
    }

    const vehicle = parkedVehicles[vehicleNumber];

    if (!vehicle) {

        showMessage(
            "Vehicle is not currently parked."
        );

        return;
    }

    const slot =
        document.querySelector(
            '[data-slot="' + vehicle.slot + '"]'
        );

    slot.classList.remove("occupied");
    slot.classList.add("available");

    const currentTime = new Date();

    const duration =
        currentTime - vehicle.entryTime;

    let hours =
        Math.ceil(duration / (1000 * 60 * 60));

    if (hours < 1) {
        hours = 1;
    }

    const rate =
        vehicle.type === "CAR" ? 50 : 20;

    const fee = hours * rate;

    delete parkedVehicles[vehicleNumber];

    updateAvailableCount();

    showMessage(
        vehicleNumber +
        " exited from " +
        vehicle.slot +
        ". Parking fee: ₹" +
        fee
    );

    document.getElementById("exitNumber").value = "";
}

function updateAvailableCount() {

    const available =
        document.querySelectorAll(
            ".slot.available"
        ).length;

    document.getElementById(
        "availableCount"
    ).textContent = available;
}

function showMessage(text) {

    const message =
        document.getElementById("message");

    message.textContent = text;
    message.style.display = "block";

    setTimeout(function () {
        message.style.display = "none";
    }, 4000);
}
