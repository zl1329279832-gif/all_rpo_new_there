const fs = require('fs');
const path = require('path');

const rootDir = path.resolve(__dirname, '..');

function writeFile(filePath, content) {
  const fullPath = path.join(rootDir, filePath);
  const dir = path.dirname(fullPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  fs.writeFileSync(fullPath, content, 'utf8');
  console.log(`Created: ${filePath}`);
}

module.exports = { writeFile, rootDir };
