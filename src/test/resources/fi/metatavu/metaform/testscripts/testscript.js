var sheetId = xlsxServices.getSheetIds()[0];
var rowCount = xlsxServices.getRowCount(sheetId);
var columnCount = xlsxServices.getColumnCount(sheetId, 0);
var _loop_1 = function (columnIndex) {
    var cellSource = xlsxServices.getCellSource(sheetId, 0, columnIndex);
    var cellField = cellSource ? cellSource.getField() : null;
    var fieldTitle = cellField ? String(cellField.getTitle()) : null;
    if (fieldTitle == "Table field") {
        xlsxServices.setCellValueString(sheetId, 0, columnIndex, `Table field changed to something else`);
    }
};
for (var columnIndex = 0; columnIndex < columnCount; columnIndex++) {
    _loop_1(columnIndex);
}