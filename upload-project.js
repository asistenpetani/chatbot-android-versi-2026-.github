const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const { Storage } = require('@google-cloud/storage');

async function main() {
    const zipFilename = 'asisten-petani.zip';
    console.log('Creating ZIP file: ' + zipFilename);
    
    const zip = new AdmZip();
    
    // Directories to exclude from the ZIP
    const excludeDirs = [
        'node_modules',
        '.gradle',
        'build',
        'app/build',
        '.build-outputs',
        '.idea',
        '.git',
        'chatbot-frontend/node_modules',
        'chatbot-backend/node_modules'
    ];
    
    const excludeFiles = [
        zipFilename,
        'firebase-app-distribution-key.json' // exclude service account key for security
    ];

    function addDirectoryToZip(localPath, zipPath = '') {
        const items = fs.readdirSync(localPath);
        for (const item of items) {
            const fullLocalPath = path.join(localPath, item);
            const fullZipPath = zipPath ? path.join(zipPath, item) : item;
            
            // Check if this path or its relative path matches any exclusion
            const isExcludedDir = excludeDirs.some(dir => {
                const normalizedDir = path.normalize(dir);
                return fullZipPath === normalizedDir || fullZipPath.startsWith(normalizedDir + path.sep);
            });
            
            if (isExcludedDir) {
                continue;
            }
            
            if (excludeFiles.includes(item)) {
                continue;
            }
            
            const stat = fs.statSync(fullLocalPath);
            if (stat.isDirectory()) {
                addDirectoryToZip(fullLocalPath, fullZipPath);
            } else if (stat.isFile()) {
                zip.addLocalFile(fullLocalPath, zipPath);
            }
        }
    }
    
    addDirectoryToZip('.');
    zip.writeZip(zipFilename);
    console.log('ZIP file created successfully: ' + zipFilename);
    
    // Initialize Google Cloud Storage using service account
    console.log('Initializing Google Cloud Storage...');
    const storage = new Storage({
        keyFilename: 'firebase-app-distribution-key.json',
        projectId: 'asistenpetani-ai-app'
    });
    
    const bucketName = 'asistenpetani-ai-app.firebasestorage.app';
    const destination = 'asisten-petani.zip';
    
    console.log(`Uploading ${zipFilename} to gs://${bucketName}/${destination}...`);
    
    try {
        await storage.bucket(bucketName).upload(zipFilename, {
            destination: destination,
            metadata: {
                contentType: 'application/zip'
            }
        });
        console.log('UPLOAD SUCCESSFUL!');
    } catch (err) {
        console.error('Upload to default bucket failed. Error details:', err);
        console.log('Trying alternative bucket name format...');
        try {
            const altBucketName = 'asistenpetani-ai-app.appspot.com';
            console.log(`Uploading ${zipFilename} to gs://${altBucketName}/${destination}...`);
            await storage.bucket(altBucketName).upload(zipFilename, {
                destination: destination,
                metadata: {
                    contentType: 'application/zip'
                }
            });
            console.log('UPLOAD SUCCESSFUL on alternative bucket!');
        } catch (altErr) {
            console.error('All upload attempts failed.');
            console.error('Alt error details:', altErr);
            process.exit(1);
        }
    }
}

main().catch(err => {
    console.error('Script error:', err);
    process.exit(1);
});
