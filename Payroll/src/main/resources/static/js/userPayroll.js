async function downloadPDF(elementId, startDate, endDate) {
    const { jsPDF } = window.jspdf;
    const element = document.getElementById(elementId);

    if (!element) {
        console.error("Element not found:", elementId);
        return;
    }

    // Convert modal content to canvas
    const canvas = await html2canvas(element, { scale: 2 });
    const imgData = canvas.toDataURL("image/png");

    // Create PDF
    const pdf = new jsPDF("p", "mm", "a4");
    const imgProps = pdf.getImageProperties(imgData);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

    pdf.addImage(imgData, "PNG", 0, 0, pdfWidth, pdfHeight);

    // Format dates (remove invalid filename characters)
    const formatDate = (date) => {
        if (!date) return "unknown";
        return String(date).replace(/[/\\?%*:|"<> ]/g, "-");
    };

    const safeStart = formatDate(startDate);
    const safeEnd = formatDate(endDate);

    // Dynamic filename based on pay period
    const filename = `Payslip_${safeStart}_to_${safeEnd}.pdf`;
    pdf.save(filename);
}
