// Ensure the DOM is fully loaded before running scripts
document.addEventListener('DOMContentLoaded', () => {
    console.log("book-demo.js loaded and DOMContentLoaded fired.");

    // Initialize Flatpickr for date input
    flatpickr("#demoDate", {
        dateFormat: "Y-m-d", // YYYY-MM-DD format
        altInput: true,
        altFormat: "F j, Y", // e.g., "September 15, 2024" for display
        minDate: "today", // Prevent selecting past dates
        onChange: function(selectedDates, dateStr, instance) {
            console.log("Date selected:", dateStr);
        }
    });

    // Initialize Flatpickr for time input
    flatpickr("#demoTime", {
        enableTime: true,
        noCalendar: true,
        dateFormat: "H:i", // HH:MM format (24-hour)
        altInput: true,
        altFormat: "h:i K", // e.g., "03:30 PM" for display
        time_24hr: false, // Use 12-hour format with AM/PM
        minuteIncrement: 15, // Allow selecting times in 15-minute intervals
        onChange: function(selectedDates, dateStr, instance) {
            console.log("Time selected:", dateStr);
        }
    });

    const bookDemoForm = document.getElementById('bookDemoForm');
    const demoMessage = document.getElementById('demoMessage');

    if (bookDemoForm) {
        console.log("Book Demo Form found. Attaching event listener.");
        bookDemoForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // Prevent default form submission

            console.log("Form submission initiated.");

            const formData = new FormData(bookDemoForm);
            const data = {};
            // Populate data object from form fields
            formData.forEach((value, key) => {
                data[key] = value;
            });

            // Get the raw date and time values from the Flatpickr inputs
            const rawDemoDate = document.getElementById('demoDate').value;
            const rawDemoTime = document.getElementById('demoTime').value;

            console.log("Raw Demo Date from input:", rawDemoDate);
            console.log("Raw Demo Time from input:", rawDemoTime);

            let formattedDemoDateTime = null;
            if (rawDemoDate && rawDemoTime) {
                // Combine date and time string for Date object parsing
                // Adding ":00" for seconds as Date constructor often expects it
                const combinedDateTimeString = `${rawDemoDate}T${rawDemoTime}:00`;

                // Create a Date object. This will be in the browser's local timezone.
                const demoDateTime = new Date(combinedDateTimeString);

                // Check if the date is valid
                if (!isNaN(demoDateTime.getTime())) {
                    // Format to YYYY-MM-DDTHH:MM:SS.000 (with milliseconds)
                    const year = demoDateTime.getFullYear();
                    const month = String(demoDateTime.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed
                    const day = String(demoDateTime.getDate()).padStart(2, '0');
                    const hours = String(demoDateTime.getHours()).padStart(2, '0');
                    const minutes = String(demoDateTime.getMinutes()).padStart(2, '0');
                    const seconds = String(demoDateTime.getSeconds()).padStart(2, '0');
                    const milliseconds = String(demoDateTime.getMilliseconds()).padStart(3, '0'); // Get milliseconds and pad

                    formattedDemoDateTime = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}.${milliseconds}`; // Added .milliseconds

                } else {
                    console.error("Invalid date or time selected for submission (Date object creation failed).");
                    demoMessage.textContent = "Please select a valid date and time.";
                    demoMessage.style.display = 'block';
                    demoMessage.style.backgroundColor = '#f8d7da'; // Light red for error
                    demoMessage.style.color = '#721c24'; // Dark red text
                    return; // Stop form submission
                }
            } else {
                console.error("Date or Time is missing from inputs.");
                demoMessage.textContent = "Please select both a date and a time.";
                demoMessage.style.display = 'block';
                demoMessage.style.backgroundColor = '#f8d7da'; // Light red for error
                demoMessage.style.color = '#721c24'; // Dark red text
                return; // Stop form submission
            }

            // Add the formatted date/time to the data payload
            console.log("Formatted Demo Date Time before adding to data:", formattedDemoDateTime);
            data.demoDateTime = formattedDemoDateTime;
            console.log("Data object to be sent:", data);

            try {
                const response = await fetch('http://localhost:8080/api/submit/demo', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                console.log("Response status:", response.status);

                if (response.ok) {
                    const result = await response.json();
                    console.log('Demo booked successfully:', result);
                    demoMessage.textContent = 'Demo scheduled successfully! We will contact you shortly.';
                    demoMessage.style.display = 'block';
                    demoMessage.style.backgroundColor = '#d4edda'; // Light green for success
                    demoMessage.style.color = '#155724'; // Dark green text
                    bookDemoForm.reset(); // Clear the form
                    // Optionally hide message after a few seconds
                    setTimeout(() => {
                        demoMessage.style.display = 'none';
                    }, 5000);
                } else {
                    const errorText = await response.text(); // Get raw error text
                    console.error('Failed to book demo:', response.status, errorText);
                    demoMessage.textContent = `Failed to schedule demo: ${errorText || 'Server error'}`;
                    demoMessage.style.display = 'block';
                    demoMessage.style.backgroundColor = '#f8d7da'; // Light red for error
                    demoMessage.style.color = '#721c24'; // Dark red text
                }
            } catch (error) {
                console.error('Error during fetch:', error);
                demoMessage.textContent = `An error occurred: ${error.message}`;
                demoMessage.style.display = 'block';
                demoMessage.style.backgroundColor = '#f8d7da'; // Light red for error
                demoMessage.style.color = '#721c24'; // Dark red text
            }
        });
    } else {
        console.error("Book Demo Form not found with ID 'bookDemoForm'.");
    }
});
