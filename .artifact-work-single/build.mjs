import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const sourcePath = "/Users/iwonjae/Documents/Codex/API-Deliverables/BLOOM_MVP_API_명세서_최신본.xlsx";
const outputPath = "/Users/iwonjae/Documents/Codex/2026-08-12/referenced-chatgpt-conversation-this-is-an/backend/outputs/api-spec-single-sheet/BLOOM_MVP_API_명세서_한시트_통합본.xlsx";
const source = await SpreadsheetFile.importXlsx(await FileBlob.load(sourcePath));
const workbook = Workbook.create();
const sheet = workbook.worksheets.add("전체 API 명세");
sheet.showGridLines = false;

sheet.mergeCells("A1:J2");
sheet.getRange("A1").values = [["BLOOM MVP API 명세서 — 한 시트 통합본"]];
sheet.getRange("A1:J2").format = {
  fill: "#172033",
  font: { bold: true, color: "#FFFFFF", size: 18 },
  verticalAlignment: "center",
};
sheet.mergeCells("A3:J3");
sheet.getRange("A3").values = [["기준일 2026-08-16 · Base URL /api/v1 · Bearer Access Token · AI 식단 계약 최신 반영"]];
sheet.getRange("A3:J3").format = {
  fill: "#E8F7EE",
  font: { color: "#166534", bold: true },
  verticalAlignment: "center",
};

let cursor = 5;
const sections = source.worksheets.items.filter((item) => item.name !== "요약");
for (const sourceSheet of sections) {
  const used = sourceSheet.getUsedRange();
  const values = used.values.slice(2).filter((row) => row.some((cell) => cell !== null && cell !== ""));
  if (values.length === 0) continue;

  sheet.mergeCells(`A${cursor}:J${cursor}`);
  sheet.getRange(`A${cursor}`).values = [[sourceSheet.name]];
  sheet.getRange(`A${cursor}:J${cursor}`).format = {
    fill: "#172033",
    font: { bold: true, color: "#FFFFFF", size: 13 },
    verticalAlignment: "center",
  };
  sheet.getRange(`A${cursor}:J${cursor}`).format.rowHeight = 28;
  cursor += 1;

  const padded = values.map((row) => Array.from({ length: 10 }, (_, col) => row[col] ?? null));
  const endRow = cursor + padded.length - 1;
  sheet.getRange(`A${cursor}:J${endRow}`).values = padded;
  sheet.getRange(`A${cursor}:J${cursor}`).format = {
    fill: "#2FC673",
    font: { bold: true, color: "#FFFFFF" },
    verticalAlignment: "center",
    wrapText: true,
    borders: { preset: "all", style: "thin", color: "#D1D5DB" },
  };
  if (endRow > cursor) {
    sheet.getRange(`A${cursor + 1}:J${endRow}`).format = {
      verticalAlignment: "top",
      wrapText: true,
      borders: {
        insideHorizontal: { style: "thin", color: "#E5E7EB" },
        bottom: { style: "thin", color: "#D1D5DB" },
      },
    };
    for (let row = cursor + 1; row <= endRow; row += 2) {
      sheet.getRange(`A${row}:J${row}`).format.fill = "#ECFDF3";
    }
  }
  cursor = endRow + 2;
}

const widths = [20, 18, 52, 42, 24, 46, 40, 22, 22, 46];
for (let col = 0; col < widths.length; col++) {
  sheet.getRangeByIndexes(0, col, cursor, 1).format.columnWidth = widths[col];
}
sheet.getRange(`A1:J${cursor}`).format.font.name = "Arial";
sheet.getRange(`A4:J${cursor}`).format.font.size = 10;
sheet.getRange(`A1:J${cursor}`).format.autofitRows();
sheet.freezePanes.freezeRows(3);

await fs.mkdir(new URL(".", `file://${outputPath}`).pathname, { recursive: true });
const output = await SpreadsheetFile.exportXlsx(workbook);
await output.save(outputPath);

console.log((await workbook.inspect({
  kind: "region",
  sheetId: "전체 API 명세",
  range: `A1:J${Math.min(cursor, 80)}`,
  maxChars: 9000,
})).ndjson);
console.log((await workbook.inspect({
  kind: "match",
  searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A",
  options: { useRegex: true, maxResults: 300 },
  summary: "final formula error scan",
})).ndjson);

const topPreview = await workbook.render({ sheetName: "전체 API 명세", range: "A1:J80", scale: 1, format: "png" });
await fs.writeFile(".artifact-work-single/top.png", new Uint8Array(await topPreview.arrayBuffer()));
const bottomStart = Math.max(1, cursor - 70);
const bottomPreview = await workbook.render({ sheetName: "전체 API 명세", range: `A${bottomStart}:J${cursor}`, scale: 1, format: "png" });
await fs.writeFile(".artifact-work-single/bottom.png", new Uint8Array(await bottomPreview.arrayBuffer()));
