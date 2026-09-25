import os
import logging
import json
import firebase_admin
from firebase_admin import credentials

logger = logging.getLogger("ASPEK-Backend")

def init_firebase():
    if firebase_admin._apps:
        return firebase_admin.get_app()
    
    # Check if we have a service account JSON file
    sa_path = os.environ.get("FIREBASE_SERVICE_ACCOUNT_PATH")
    if not sa_path:
        # Fallback to local standard paths
        for path in ["service-account.json", "backend_cloud/service-account.json"]:
            if os.path.exists(path):
                sa_path = path
                logger.info(f"Menemukan file kredensial lokal di: {path}")
                break

    if sa_path and os.path.exists(sa_path):
        try:
            cred = credentials.Certificate(sa_path)
            return firebase_admin.initialize_app(cred)
        except Exception as e:
            logger.error(f"Gagal memuat kredensial Firebase dari file {sa_path}: {e}")
            
    # Check if we have JSON string in environment variable
    sa_json = os.environ.get("FIREBASE_SERVICE_ACCOUNT_JSON")
    if sa_json:
        try:
            cred_dict = json.loads(sa_json)
            cred = credentials.Certificate(cred_dict)
            return firebase_admin.initialize_app(cred)
        except Exception as e:
            logger.error(f"Gagal memuat kredensial Firebase dari env FIREBASE_SERVICE_ACCOUNT_JSON: {e}")
            
    # Try using Application Default Credentials (ADC) as fallback (Works natively in Cloud Run)
    try:
        cred = credentials.ApplicationDefault()
        
        # Cloud Run needs the serviceAccountId specified if you want to generate Signed URLs using ADC.
        # It defaults to the App Engine default service account or Compute Engine default service account.
        service_account_id = os.environ.get("GCP_SERVICE_ACCOUNT_EMAIL", "asistenpetani-ai-app@appspot.gserviceaccount.com")
        
        return firebase_admin.initialize_app(cred, {
            'projectId': 'asistenpetani-ai-app',
            'serviceAccountId': service_account_id
        })
    except Exception as e:
        logger.error(f"Gagal memuat kredensial bawaan (ADC): {e}")
        
    return None
